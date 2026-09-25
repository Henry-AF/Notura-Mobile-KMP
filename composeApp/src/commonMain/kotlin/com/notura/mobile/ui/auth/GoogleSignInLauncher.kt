package com.notura.mobile.ui.auth

/** Result of the platform's native Google account picker (Figma "sign in - google", 2270:208). */
sealed interface GoogleIdTokenResult {
    data class Success(val idToken: String, val nonce: String?) : GoogleIdTokenResult

    /** The user closed the picker; nothing is shown. */
    data object Cancelled : GoogleIdTokenResult

    data object Failed : GoogleIdTokenResult
}

/**
 * Obtains a Google ID token natively (Credential Manager on Android, Google Sign-In on iOS).
 * The token is then exchanged for a Supabase session by the AuthRepository; no auth logic here.
 */
fun interface GoogleSignInLauncher {
    suspend fun requestIdToken(): GoogleIdTokenResult
}
