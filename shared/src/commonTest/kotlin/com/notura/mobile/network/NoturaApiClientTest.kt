package com.notura.mobile.network

import com.notura.mobile.testing.FakeTokenProvider
import com.notura.mobile.testing.MockApi
import com.notura.mobile.testing.TEST_BASE_URL
import com.notura.mobile.testing.respondJson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class NoturaApiClientTest {

    @Serializable
    private data class Ping(val ok: Boolean)

    @Test
    fun sendsBearerTokenAndDecodesSuccessBody() = runTest {
        val api = MockApi { respondJson("""{"ok":true,"extra":"ignored"}""") }

        val result = api.client.get("/api/ping", Ping.serializer())

        assertEquals(ApiResult.Success(Ping(ok = true)), result)
        assertEquals("$TEST_BASE_URL/api/ping", api.requests.single().url.toString())
        assertEquals("Bearer token-1", api.requests.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun omitsAuthorizationHeaderWhenThereIsNoSession() = runTest {
        val api = MockApi(tokens = FakeTokenProvider(token = null)) { respondJson("""{"ok":true}""") }

        api.client.get("/api/ping", Ping.serializer())

        assertNull(api.requests.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun refreshesOnceAndRetriesAfter401() = runTest {
        val tokens = FakeTokenProvider(token = "expired", refreshedToken = "fresh")
        val api = MockApi(tokens) { request ->
            if (request.headers[HttpHeaders.Authorization] == "Bearer fresh") {
                respondJson("""{"ok":true}""")
            } else {
                respondJson("""{"error":"Não autenticado."}""", HttpStatusCode.Unauthorized)
            }
        }

        val result = api.client.get("/api/ping", Ping.serializer())

        assertEquals(ApiResult.Success(Ping(ok = true)), result)
        assertEquals(1, tokens.refreshCount)
        assertEquals(listOf("Bearer expired", "Bearer fresh"), api.requests.map { it.headers[HttpHeaders.Authorization] })
    }

    @Test
    fun returnsUnauthorizedWhenRefreshIsNotPossible() = runTest {
        val tokens = FakeTokenProvider(token = "expired", refreshedToken = null)
        val api = MockApi(tokens) { respondJson("""{"error":"Não autenticado."}""", HttpStatusCode.Unauthorized) }

        val result = api.client.get("/api/ping", Ping.serializer())

        assertEquals(ApiResult.Failure(ApiError.Unauthorized("Não autenticado.")), result)
        assertEquals(1, api.requests.size)
    }

    @Test
    fun returnsUnauthorizedWhenRetriedRequestIsStillRejected() = runTest {
        val tokens = FakeTokenProvider(token = "expired", refreshedToken = "also-rejected")
        val api = MockApi(tokens) { respondJson("""{"error":"Não autenticado."}""", HttpStatusCode.Unauthorized) }

        val result = api.client.get("/api/ping", Ping.serializer())

        assertEquals(ApiResult.Failure(ApiError.Unauthorized("Não autenticado.")), result)
        assertEquals(2, api.requests.size)
        assertEquals(1, tokens.refreshCount)
    }

    @Test
    fun mapsForbiddenWithQuotaFields() = runTest {
        val api = MockApi {
            respondJson("""{"error":"ai_chat_daily_quota_exceeded","quotaLimit":10}""", HttpStatusCode.Forbidden)
        }

        val result = api.client.get("/api/ping", Ping.serializer())

        assertEquals(
            ApiResult.Failure(ApiError.Forbidden("ai_chat_daily_quota_exceeded", code = null, quotaLimit = 10)),
            result,
        )
    }

    @Test
    fun mapsRateLimitWithRetryAfterHeader() = runTest {
        val api = MockApi {
            respondJson(
                """{"error":"Muitas requisições. Tente novamente em instantes.","code":"rate_limited"}""",
                HttpStatusCode.TooManyRequests,
                mapOf(HttpHeaders.RetryAfter to "37"),
            )
        }

        val result = api.client.get("/api/ping", Ping.serializer())

        assertEquals(
            ApiResult.Failure(
                ApiError.RateLimited("Muitas requisições. Tente novamente em instantes.", retryAfterSeconds = 37, code = "rate_limited"),
            ),
            result,
        )
    }

    @Test
    fun mapsRateLimitWithoutRetryAfterHeader() = runTest {
        val api = MockApi {
            respondJson("""{"error":"Limite de reenvios atingido (3 por reunião)."}""", HttpStatusCode.TooManyRequests)
        }

        val result = api.client.get("/api/ping", Ping.serializer())

        assertEquals(
            ApiResult.Failure(ApiError.RateLimited("Limite de reenvios atingido (3 por reunião).", retryAfterSeconds = null)),
            result,
        )
    }

    @Test
    fun mapsDocumentedStatusCodes() = runTest {
        val expectations = mapOf(
            HttpStatusCode.BadRequest to ApiError.InvalidRequest(400, "msg"),
            HttpStatusCode.NotFound to ApiError.NotFound("msg"),
            HttpStatusCode.Conflict to ApiError.Conflict("msg"),
            HttpStatusCode.PayloadTooLarge to ApiError.InvalidRequest(413, "msg"),
            HttpStatusCode.UnsupportedMediaType to ApiError.InvalidRequest(415, "msg"),
            HttpStatusCode.UnprocessableEntity to ApiError.InvalidRequest(422, "msg"),
            HttpStatusCode.InternalServerError to ApiError.Server(500, "msg"),
            HttpStatusCode.BadGateway to ApiError.Server(502, "msg"),
            HttpStatusCode.ServiceUnavailable to ApiError.Server(503, "msg"),
            HttpStatusCode.Gone to ApiError.Unexpected(410, "msg"),
        )

        for ((status, expected) in expectations) {
            val api = MockApi { respondJson("""{"error":"msg"}""", status) }
            assertEquals(ApiResult.Failure(expected), api.client.get("/api/ping", Ping.serializer()), "status $status")
        }
    }

    @Test
    fun toleratesErrorResponsesWithoutJsonBody() = runTest {
        val api = MockApi { respondJson("<html>Bad gateway</html>", HttpStatusCode.BadGateway) }

        val result = api.client.get("/api/ping", Ping.serializer())

        assertEquals(ApiResult.Failure(ApiError.Server(502, null)), result)
    }

    @Test
    fun reportsSuccessBodyThatDoesNotMatchContract() = runTest {
        val api = MockApi { respondJson("""{"unexpected":1}""") }

        val result = api.client.get("/api/ping", Ping.serializer())

        val error = assertIs<ApiError.Unexpected>(assertIs<ApiResult.Failure>(result).error)
        assertEquals(200, error.status)
    }

    @Test
    fun reportsNetworkFailures() = runTest {
        val api = MockApi { throw IllegalStateException("offline") }

        val result = api.client.get("/api/ping", Ping.serializer())

        val error = assertIs<ApiError.Network>(assertIs<ApiResult.Failure>(result).error)
        assertEquals("offline", error.message)
    }
}
