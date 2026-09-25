package com.notura.mobile.testing

import com.notura.mobile.network.AccessTokenProvider
import com.notura.mobile.network.NoturaApiClient
import com.notura.mobile.network.createNoturaHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

const val TEST_BASE_URL = "https://api.notura.test"

class FakeTokenProvider(
    var token: String? = "token-1",
    private val refreshedToken: String? = null,
) : AccessTokenProvider {
    var refreshCount = 0
        private set

    override suspend fun currentAccessToken(): String? = token

    override suspend fun refreshAccessToken(): String? {
        refreshCount++
        refreshedToken?.let { token = it }
        return refreshedToken
    }
}

/** A [NoturaApiClient] backed by a MockEngine; [requests] records every call made. */
class MockApi(
    val tokens: FakeTokenProvider = FakeTokenProvider(),
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
) {
    val requests = mutableListOf<HttpRequestData>()
    val client = NoturaApiClient(
        httpClient = createNoturaHttpClient(
            MockEngine { request ->
                requests += request
                handler(request)
            },
        ),
        baseUrl = TEST_BASE_URL,
        tokens = tokens,
    )
}

fun MockRequestHandleScope.respondJson(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
    extraHeaders: Map<String, String> = emptyMap(),
): HttpResponseData {
    val headers = Headers.build {
        append(HttpHeaders.ContentType, "application/json")
        extraHeaders.forEach { (key, value) -> append(key, value) }
    }
    return respond(body, status, headers)
}
