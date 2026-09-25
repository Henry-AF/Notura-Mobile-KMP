package com.notura.mobile.data.auth

import com.notura.mobile.domain.auth.AuthFailure
import com.notura.mobile.domain.auth.AuthResult
import com.notura.mobile.domain.auth.AuthSessionState
import com.notura.mobile.domain.auth.SignUpOutcome
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthRepository(
    private val auth: Auth,
    scope: CoroutineScope,
) : AuthRepository {

    override val sessionState: StateFlow<AuthSessionState> = auth.sessionStatus
        .map { it.toSessionState() }
        .stateIn(scope, SharingStarted.Eagerly, auth.sessionStatus.value.toSessionState())

    override suspend fun signIn(email: String, password: String): AuthResult<Unit> = runAuth {
        auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    override suspend fun signUp(fullName: String, email: String, password: String): AuthResult<SignUpOutcome> =
        runAuth {
            val normalizedEmail = email.trim()
            auth.signUpWith(Email) {
                this.email = normalizedEmail
                this.password = password
                data = buildJsonObject { put(FULL_NAME_METADATA_KEY, fullName.trim()) }
            }
            if (auth.currentSessionOrNull() != null) {
                SignUpOutcome.SignedIn
            } else {
                SignUpOutcome.ConfirmationEmailSent(normalizedEmail)
            }
        }

    override suspend fun signInWithGoogleIdToken(idToken: String, nonce: String?): AuthResult<Unit> = runAuth {
        auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
            this.nonce = nonce
        }
    }

    override suspend fun signOut() {
        // supabase-kt keeps the local session when the logout request never reaches the server
        // (offline). Signing out must always work on the device, so clear it ourselves.
        if (runAuth { auth.signOut() } is AuthResult.Failure) {
            auth.clearSession()
        }
    }

    override suspend fun currentAccessToken(): String? = auth.currentAccessTokenOrNull()

    override suspend fun refreshAccessToken(): String? {
        if (auth.currentSessionOrNull() == null) return null
        return when (runAuth { auth.refreshCurrentSession() }) {
            is AuthResult.Success -> auth.currentAccessTokenOrNull()
            is AuthResult.Failure -> null
        }
    }

    private companion object {
        const val FULL_NAME_METADATA_KEY = "full_name"
    }
}

private fun SessionStatus.toSessionState(): AuthSessionState = when (this) {
    is SessionStatus.Initializing -> AuthSessionState.Loading
    is SessionStatus.Authenticated -> AuthSessionState.SignedIn(
        userId = session.user?.id.orEmpty(),
        email = session.user?.email,
    )
    // The stored session is kept while a refresh is retried; the user remains signed in.
    is SessionStatus.RefreshFailure -> AuthSessionState.Loading
    is SessionStatus.NotAuthenticated -> AuthSessionState.SignedOut
}

private suspend fun <T> runAuth(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (error: Exception) {
    AuthResult.Failure(error.toAuthFailure())
}

internal fun Exception.toAuthFailure(): AuthFailure = when (this) {
    is AuthWeakPasswordException -> AuthFailure.WeakPassword(reasons)
    is AuthRestException -> errorCode.toAuthFailure(errorDescription)
    is RestException -> if (response.status.value == 429) AuthFailure.RateLimited else AuthFailure.Unknown(description)
    is HttpRequestException -> AuthFailure.Network
    else -> AuthFailure.Unknown(message)
}

private fun AuthErrorCode?.toAuthFailure(description: String): AuthFailure = when (this) {
    AuthErrorCode.InvalidCredentials -> AuthFailure.InvalidCredentials
    AuthErrorCode.EmailNotConfirmed -> AuthFailure.EmailNotConfirmed
    AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> AuthFailure.EmailAlreadyRegistered
    AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit -> AuthFailure.RateLimited
    else -> AuthFailure.Unknown(description)
}
