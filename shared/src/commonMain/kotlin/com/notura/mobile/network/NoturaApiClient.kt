package com.notura.mobile.network

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable

/**
 * Authenticated client for the Notura API.
 *
 * Sends the Bearer token on every request. On a 401 it refreshes the session once and retries,
 * as the contract's checklist requires (section 7).
 */
class NoturaApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val tokens: AccessTokenProvider,
) {

    suspend fun <T> get(path: String, deserializer: DeserializationStrategy<T>): ApiResult<T> =
        send(HttpMethod.Get, path, deserializer)

    suspend fun <T> send(
        method: HttpMethod,
        path: String,
        deserializer: DeserializationStrategy<T>,
        configure: HttpRequestBuilder.() -> Unit = {},
    ): ApiResult<T> {
        val response = try {
            executeWithRefresh(method, path, configure)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            return ApiResult.Failure(ApiError.Network(error))
        }
        if (!response.status.isSuccess()) return ApiResult.Failure(response.toApiError())
        return response.decode(deserializer)
    }

    private suspend fun executeWithRefresh(
        method: HttpMethod,
        path: String,
        configure: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        val response = execute(method, path, tokens.currentAccessToken(), configure)
        if (response.status != HttpStatusCode.Unauthorized) return response
        val refreshedToken = tokens.refreshAccessToken() ?: return response
        return execute(method, path, refreshedToken, configure)
    }

    private suspend fun execute(
        method: HttpMethod,
        path: String,
        token: String?,
        configure: HttpRequestBuilder.() -> Unit,
    ): HttpResponse = httpClient.request {
        this.method = method
        url(baseUrl.trimEnd('/') + path)
        token?.let { bearerAuth(it) }
        configure()
    }
}

private suspend fun <T> HttpResponse.decode(deserializer: DeserializationStrategy<T>): ApiResult<T> {
    val status = status.value
    return try {
        ApiResult.Success(NoturaJson.decodeFromString(deserializer, bodyAsText()))
    } catch (error: SerializationException) {
        ApiResult.Failure(ApiError.Unexpected(status, error.message))
    } catch (error: IllegalArgumentException) {
        ApiResult.Failure(ApiError.Unexpected(status, error.message))
    }
}

internal suspend fun HttpResponse.toApiError(): ApiError {
    val body = readErrorBody()
    val message = body?.error
    return when (val code = status.value) {
        401 -> ApiError.Unauthorized(message)
        403 -> ApiError.Forbidden(message, body?.code, body?.quotaLimit)
        404 -> ApiError.NotFound(message)
        409 -> ApiError.Conflict(message)
        429 -> ApiError.RateLimited(message, headers[HttpHeaders.RetryAfter]?.trim()?.toLongOrNull(), body?.code)
        400, 413, 415, 422 -> ApiError.InvalidRequest(code, message)
        in 500..599 -> ApiError.Server(code, message)
        else -> ApiError.Unexpected(code, message)
    }
}

/** Error payload shared by the API routes: `{ "error": "...", "code"?: "...", "quotaLimit"?: 10 }`. */
@Serializable
private data class ApiErrorBody(
    val error: String? = null,
    val code: String? = null,
    val quotaLimit: Int? = null,
)

private suspend fun HttpResponse.readErrorBody(): ApiErrorBody? {
    val text = try {
        bodyAsText()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        return null
    }
    if (text.isBlank()) return null
    return try {
        NoturaJson.decodeFromString(ApiErrorBody.serializer(), text)
    } catch (error: SerializationException) {
        null
    } catch (error: IllegalArgumentException) {
        null
    }
}
