package com.notura.mobile.data.auth

import com.notura.mobile.domain.auth.AuthFailure
import com.notura.mobile.domain.auth.AuthResult
import com.notura.mobile.domain.auth.AuthSessionState
import com.notura.mobile.domain.auth.SignUpOutcome
import com.notura.mobile.testing.respondJson
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val SUPABASE_URL = "https://project.supabase.test"

private const val USER_JSON = """
{
  "id": "user-1",
  "aud": "authenticated",
  "role": "authenticated",
  "email": "ana@example.com",
  "app_metadata": {"provider": "email"},
  "user_metadata": {"full_name": "Ana"},
  "created_at": "2026-09-25T12:00:00Z"
}
"""

private fun sessionJson(accessToken: String) = """
{
  "access_token": "$accessToken",
  "token_type": "bearer",
  "expires_in": 3600,
  "expires_at": 4102444800,
  "refresh_token": "refresh-1",
  "user": $USER_JSON
}
"""

private fun goTrueError(status: Int, code: String, message: String, extra: String = "") =
    """{"code":$status,"error_code":"$code","msg":"$message"$extra}"""

private class AuthHarness(
    scope: CoroutineScope,
    private val handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
) {
    val requests = mutableListOf<HttpRequestData>()
    private val client = createNoturaSupabaseClient(
        config = SupabaseConfig(SUPABASE_URL, "anon-key"),
        sessionManager = MemorySessionManager(),
        httpEngine = MockEngine { request ->
            requests += request
            handler(request)
        },
    )
    val repository = SupabaseAuthRepository(client.auth, scope)

    fun bodyOf(index: Int): JsonObject =
        Json.parseToJsonElement((requests[index].body as TextContent).text).jsonObject
}

private fun TestScope.harness(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
) = AuthHarness(backgroundScope, handler)

@OptIn(ExperimentalCoroutinesApi::class)
class SupabaseAuthRepositoryTest {

    @Test
    fun signInWithPasswordStoresSessionAndExposesAccessToken() = runTest {
        val auth = harness { respondJson(sessionJson("access-1")) }

        val result = auth.repository.signIn(" ana@example.com ", "s3cret-pass")

        assertEquals(AuthResult.Success(Unit), result)
        val request = auth.requests.single()
        assertEquals("/auth/v1/token", request.url.encodedPath)
        assertEquals("password", request.url.parameters["grant_type"])
        assertEquals("ana@example.com", auth.bodyOf(0)["email"]?.jsonPrimitive?.content)
        assertEquals("access-1", auth.repository.currentAccessToken())
        runCurrent()
        assertEquals(AuthSessionState.SignedIn("user-1", "ana@example.com"), auth.repository.sessionState.value)
    }

    @Test
    fun signInWithWrongPasswordIsInvalidCredentials() = runTest {
        val auth = harness {
            respondJson(goTrueError(400, "invalid_credentials", "Invalid login credentials"), HttpStatusCode.BadRequest)
        }

        val result = auth.repository.signIn("ana@example.com", "wrong")

        assertEquals(AuthResult.Failure(AuthFailure.InvalidCredentials("Invalid login credentials")), result)
        assertEquals(null, auth.repository.currentAccessToken())
    }

    @Test
    fun signInBeforeConfirmingEmailIsEmailNotConfirmed() = runTest {
        val auth = harness {
            respondJson(goTrueError(400, "email_not_confirmed", "Email not confirmed"), HttpStatusCode.BadRequest)
        }

        assertEquals(AuthResult.Failure(AuthFailure.EmailNotConfirmed("Email not confirmed")), auth.repository.signIn("ana@example.com", "pw"))
    }

    @Test
    fun signInRateLimited() = runTest {
        val auth = harness {
            respondJson(
                goTrueError(429, "over_request_rate_limit", "Request rate limit reached"),
                HttpStatusCode.TooManyRequests,
            )
        }

        assertEquals(AuthResult.Failure(AuthFailure.RateLimited("Request rate limit reached")), auth.repository.signIn("ana@example.com", "pw"))
    }

    @Test
    fun signInWithoutConnectivityIsNetworkFailure() = runTest {
        val auth = harness { throw IOException("offline") }

        assertEquals(AuthResult.Failure(AuthFailure.Network), auth.repository.signIn("ana@example.com", "pw"))
    }

    @Test
    fun signUpSendsFullNameMetadataAndReportsConfirmationEmail() = runTest {
        val auth = harness { respondJson(USER_JSON) }

        val result = auth.repository.signUp(" Ana ", "ana@example.com", "s3cret-pass")

        assertEquals(AuthResult.Success(SignUpOutcome.ConfirmationEmailSent("ana@example.com")), result)
        assertEquals("/auth/v1/signup", auth.requests.single().url.encodedPath)
        val data = auth.bodyOf(0)["data"]?.jsonObject
        assertEquals("Ana", data?.get("full_name")?.jsonPrimitive?.content)
        runCurrent()
        assertEquals(AuthSessionState.SignedOut, auth.repository.sessionState.value)
    }

    @Test
    fun signUpWithoutEmailConfirmationSignsIn() = runTest {
        val auth = harness { respondJson(sessionJson("access-2")) }

        val result = auth.repository.signUp("Ana", "ana@example.com", "s3cret-pass")

        assertEquals(AuthResult.Success(SignUpOutcome.SignedIn), result)
        assertEquals("access-2", auth.repository.currentAccessToken())
    }

    @Test
    fun signUpWithExistingEmail() = runTest {
        val auth = harness {
            respondJson(goTrueError(422, "user_already_exists", "User already registered"), HttpStatusCode.UnprocessableEntity)
        }

        assertEquals(
            AuthResult.Failure(AuthFailure.EmailAlreadyRegistered("User already registered")),
            auth.repository.signUp("Ana", "ana@example.com", "s3cret-pass"),
        )
    }

    @Test
    fun signUpWithWeakPasswordReturnsReasons() = runTest {
        val auth = harness {
            respondJson(
                goTrueError(422, "weak_password", "Password should be at least 8 characters.", ""","weak_password":{"reasons":["length"]}"""),
                HttpStatusCode.UnprocessableEntity,
            )
        }

        assertEquals(
            AuthResult.Failure(AuthFailure.WeakPassword(listOf("length"), "Password should be at least 8 characters.")),
            auth.repository.signUp("Ana", "ana@example.com", "short"),
        )
    }

    @Test
    fun unknownErrorCodeKeepsServerDescription() = runTest {
        val auth = harness {
            respondJson(goTrueError(422, "signup_disabled", "Signups not allowed for this instance"), HttpStatusCode.UnprocessableEntity)
        }

        val failure = assertIs<AuthResult.Failure>(auth.repository.signUp("Ana", "ana@example.com", "s3cret-pass")).failure

        assertEquals(AuthFailure.Unknown("Signups not allowed for this instance"), failure)
    }

    @Test
    fun googleIdTokenIsExchangedForSession() = runTest {
        val auth = harness { respondJson(sessionJson("access-google")) }

        val result = auth.repository.signInWithGoogleIdToken("google-id-token", nonce = "nonce-1")

        assertEquals(AuthResult.Success(Unit), result)
        val request = auth.requests.single()
        assertEquals("id_token", request.url.parameters["grant_type"])
        val body = auth.bodyOf(0)
        assertEquals("google-id-token", body["id_token"]?.jsonPrimitive?.content)
        assertEquals("google", body["provider"]?.jsonPrimitive?.content)
        assertEquals("nonce-1", body["nonce"]?.jsonPrimitive?.content)
        assertEquals("access-google", auth.repository.currentAccessToken())
    }

    @Test
    fun refreshAccessTokenUsesRefreshTokenGrant() = runTest {
        var tokenCalls = 0
        val auth = harness {
            tokenCalls++
            respondJson(sessionJson(if (tokenCalls == 1) "access-1" else "access-refreshed"))
        }
        auth.repository.signIn("ana@example.com", "s3cret-pass")

        val refreshed = auth.repository.refreshAccessToken()

        assertEquals("access-refreshed", refreshed)
        assertEquals("refresh_token", auth.requests.last().url.parameters["grant_type"])
        assertEquals("refresh-1", auth.bodyOf(1)["refresh_token"]?.jsonPrimitive?.content)
    }

    @Test
    fun refreshAccessTokenWithoutSessionReturnsNullWithoutCallingServer() = runTest {
        val auth = harness { error("no request expected") }

        assertEquals(null, auth.repository.refreshAccessToken())
        assertEquals(0, auth.requests.size)
    }

    @Test
    fun refreshFailureReturnsNull() = runTest {
        var tokenCalls = 0
        val auth = harness {
            tokenCalls++
            if (tokenCalls == 1) {
                respondJson(sessionJson("access-1"))
            } else {
                respondJson(goTrueError(400, "refresh_token_not_found", "Invalid Refresh Token"), HttpStatusCode.BadRequest)
            }
        }
        auth.repository.signIn("ana@example.com", "s3cret-pass")

        assertEquals(null, auth.repository.refreshAccessToken())
    }

    @Test
    fun signOutClearsLocalSessionEvenWhenServerLogoutFails() = runTest {
        var calls = 0
        val auth = harness {
            calls++
            if (calls == 1) respondJson(sessionJson("access-1")) else throw IOException("offline")
        }
        auth.repository.signIn("ana@example.com", "s3cret-pass")

        auth.repository.signOut()

        assertEquals(null, auth.repository.currentAccessToken())
        runCurrent()
        assertEquals(AuthSessionState.SignedOut, auth.repository.sessionState.value)
    }
}
