package com.notura.mobile.domain.auth

/** Session state exposed to the UI. */
sealed interface AuthSessionState {
    /** Restoring a stored session at startup. */
    data object Loading : AuthSessionState

    data class SignedIn(val userId: String, val email: String?) : AuthSessionState

    data object SignedOut : AuthSessionState
}

/** Why a Supabase Auth call failed, reduced to the cases the auth screens handle. */
sealed interface AuthFailure {
    data object InvalidCredentials : AuthFailure

    data object EmailNotConfirmed : AuthFailure

    data object EmailAlreadyRegistered : AuthFailure

    data class WeakPassword(val reasons: List<String>) : AuthFailure

    data object RateLimited : AuthFailure

    data object Network : AuthFailure

    /** Any other Supabase Auth error; [message] is the server's description, in English. */
    data class Unknown(val message: String?) : AuthFailure
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
