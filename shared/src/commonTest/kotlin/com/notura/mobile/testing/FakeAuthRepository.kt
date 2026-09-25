package com.notura.mobile.testing

import com.notura.mobile.data.auth.AuthRepository
import com.notura.mobile.domain.auth.AuthResult
import com.notura.mobile.domain.auth.AuthSessionState
import com.notura.mobile.domain.auth.SignUpOutcome
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Auth repository whose calls suspend until the test completes [nextSignIn] / [nextSignUp]. */
class FakeAuthRepository : AuthRepository {
    override val sessionState: StateFlow<AuthSessionState> = MutableStateFlow(AuthSessionState.SignedOut)

    var nextSignIn = CompletableDeferred<AuthResult<Unit>>()
    var nextSignUp = CompletableDeferred<AuthResult<SignUpOutcome>>()
    val signInCalls = mutableListOf<Pair<String, String>>()
    val signUpCalls = mutableListOf<Triple<String, String, String>>()
    val googleCalls = mutableListOf<Pair<String, String?>>()

    override suspend fun signIn(email: String, password: String): AuthResult<Unit> {
        signInCalls += email to password
        return nextSignIn.await()
    }

    override suspend fun signUp(fullName: String, email: String, password: String): AuthResult<SignUpOutcome> {
        signUpCalls += Triple(fullName, email, password)
        return nextSignUp.await()
    }

    override suspend fun signInWithGoogleIdToken(idToken: String, nonce: String?): AuthResult<Unit> {
        googleCalls += idToken to nonce
        return nextSignIn.await()
    }

    override suspend fun signOut() = Unit

    override suspend fun currentAccessToken(): String? = null

    override suspend fun refreshAccessToken(): String? = null
}
