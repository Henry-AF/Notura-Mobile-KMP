package com.notura.mobile.data.auth

import com.notura.mobile.domain.auth.AuthResult
import com.notura.mobile.domain.auth.AuthSessionState
import com.notura.mobile.domain.auth.SignUpOutcome
import com.notura.mobile.network.AccessTokenProvider
import kotlinx.coroutines.flow.StateFlow

/**
 * Login, sign-up and session handling against Supabase Auth (contract section 1.1). The API has
 * no login endpoint; the session's access token is what [AccessTokenProvider] hands to the API client.
 */
interface AuthRepository : AccessTokenProvider {
    val sessionState: StateFlow<AuthSessionState>

    suspend fun signIn(email: String, password: String): AuthResult<Unit>

    /** Sends [fullName] as the `full_name` user metadata, as the web sign-up does. */
    suspend fun signUp(fullName: String, email: String, password: String): AuthResult<SignUpOutcome>

    /** Exchanges a Google ID token obtained natively on the device for a Supabase session. */
    suspend fun signInWithGoogleIdToken(idToken: String, nonce: String?): AuthResult<Unit>

    suspend fun signOut()
}
