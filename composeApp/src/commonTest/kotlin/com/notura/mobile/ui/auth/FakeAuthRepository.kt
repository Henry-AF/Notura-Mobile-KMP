package com.notura.mobile.ui.auth

import com.notura.mobile.data.auth.AuthRepository
import com.notura.mobile.domain.auth.AuthResult
import com.notura.mobile.domain.auth.AuthSessionState
import com.notura.mobile.domain.auth.SignUpOutcome
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

/** Auth repository whose calls suspend until the test completes [nextSignIn] / [nextSignUp]. */
class FakeAuthRepository : AuthRepository {
    override val sessionState = MutableStateFlow<AuthSessionState>(AuthSessionState.SignedOut)

    val nextSignIn = CompletableDeferred<AuthResult<Unit>>()
    val nextSignUp = CompletableDeferred<AuthResult<SignUpOutcome>>()
    val signInCalls = mutableListOf<Pair<String, String>>()
    val signUpCalls = mutableListOf<Triple<String, String, String>>()
    val googleCalls = mutableListOf<String>()

    override suspend fun signIn(email: String, password: String): AuthResult<Unit> {
        signInCalls += email to password
        return nextSignIn.await()
    }

    override suspend fun signUp(fullName: String, email: String, password: String): AuthResult<SignUpOutcome> {
        signUpCalls += Triple(fullName, email, password)
        return nextSignUp.await()
    }

    override suspend fun signInWithGoogleIdToken(idToken: String, nonce: String?): AuthResult<Unit> {
        googleCalls += idToken
        return nextSignIn.await()
    }

    override suspend fun signOut() = Unit

    override suspend fun currentAccessToken(): String? = null

    override suspend fun refreshAccessToken(): String? = null
}
