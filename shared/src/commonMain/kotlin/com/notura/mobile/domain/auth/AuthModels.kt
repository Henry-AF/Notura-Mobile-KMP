package com.notura.mobile.domain.auth

/** Session state exposed to the UI. */
sealed interface AuthSessionState {
    /** Restoring a stored session at startup. */
    data object Loading : AuthSessionState

    data class SignedIn(val userId: String, val email: String?) : AuthSessionState

    data object SignedOut : AuthSessionState
}

/**
 * Why a Supabase Auth call failed, reduced to the cases the auth screens handle.
 * [serverMessage] is Supabase's own description (in English), which the web shows as-is.
 */
sealed interface AuthFailure {
    val serverMessage: String?

    data class InvalidCredentials(override val serverMessage: String?) : AuthFailure

    data class EmailNotConfirmed(override val serverMessage: String?) : AuthFailure

    data class EmailAlreadyRegistered(override val serverMessage: String?) : AuthFailure

    data class WeakPassword(val reasons: List<String>, override val serverMessage: String?) : AuthFailure

    data class RateLimited(override val serverMessage: String?) : AuthFailure

    data object Network : AuthFailure {
        override val serverMessage: String? = null
    }

    /** Any other Supabase Auth error. */
    data class Unknown(override val serverMessage: String?) : AuthFailure
}

sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Failure(val failure: AuthFailure) : AuthResult<Nothing>
}

/** Supabase returns a session on sign-up only when e-mail confirmation is disabled for the project. */
sealed interface SignUpOutcome {
    data object SignedIn : SignUpOutcome

    data class ConfirmationEmailSent(val email: String) : SignUpOutcome
}
