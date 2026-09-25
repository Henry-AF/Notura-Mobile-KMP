package com.notura.mobile.presentation.auth

import com.notura.mobile.domain.auth.AuthFailure
import com.notura.mobile.domain.auth.AuthResult
import com.notura.mobile.testing.FakeAuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SignInStateHolderTest {

    private val repository = FakeAuthRepository()

    private fun TestScope.holder() = SignInStateHolder(repository, backgroundScope)

    private fun SignInStateHolder.fill(email: String = "ana@example.com", password: String = "s3cret") {
        onEmailChange(email)
        onPasswordChange(password)
    }

    @Test
    fun submitGoesLoadingThenSuccess() = runTest {
        val holder = holder()
        holder.fill()

        holder.onSubmit()
        runCurrent()

        assertTrue(holder.state.value.isSubmitting)
        assertEquals(listOf("ana@example.com" to "s3cret"), repository.signInCalls)

        repository.nextSignIn.complete(AuthResult.Success(Unit))
        runCurrent()

        assertFalse(holder.state.value.isSubmitting)
        assertEquals(null, holder.state.value.authFailure)
    }

    @Test
    fun submitGoesLoadingThenError() = runTest {
        val holder = holder()
        holder.fill()

        holder.onSubmit()
        runCurrent()
        repository.nextSignIn.complete(AuthResult.Failure(AuthFailure.InvalidCredentials))
        runCurrent()

        assertFalse(holder.state.value.isSubmitting)
        assertEquals(AuthFailure.InvalidCredentials, holder.state.value.authFailure)
    }

    @Test
    fun editingAFieldClearsTheAuthError() = runTest {
        val holder = holder()
        holder.fill()
        holder.onSubmit()
        runCurrent()
        repository.nextSignIn.complete(AuthResult.Failure(AuthFailure.InvalidCredentials))
        runCurrent()

        holder.onPasswordChange("another")

        assertEquals(null, holder.state.value.authFailure)
    }

    @Test
    fun invalidFormShowsFieldErrorsWithoutCallingRepository() = runTest {
        val holder = holder()
        holder.fill(email = "not-an-email", password = "")

        holder.onSubmit()
        runCurrent()

        assertEquals(FieldError.EmailInvalid, holder.state.value.emailError)
        assertEquals(FieldError.PasswordRequired, holder.state.value.passwordError)
        assertFalse(holder.state.value.isSubmitting)
        assertTrue(repository.signInCalls.isEmpty())
    }

    @Test
    fun emptyEmailIsRequired() = runTest {
        val holder = holder()
        holder.fill(email = "   ")

        holder.onSubmit()

        assertEquals(FieldError.EmailRequired, holder.state.value.emailError)
    }

    @Test
    fun ignoresSecondSubmitWhileRequestIsInFlight() = runTest {
        val holder = holder()
        holder.fill()

        holder.onSubmit()
        runCurrent()
        holder.onSubmit()
        runCurrent()

        assertEquals(1, repository.signInCalls.size)
    }

    @Test
    fun googleIdTokenGoesLoadingThenError() = runTest {
        val holder = holder()

        holder.onGoogleIdToken("id-token", "nonce")
        runCurrent()

        assertTrue(holder.state.value.isGoogleSubmitting)
        assertEquals(listOf<Pair<String, String?>>("id-token" to "nonce"), repository.googleCalls)

        repository.nextSignIn.complete(AuthResult.Failure(AuthFailure.Network))
        runCurrent()

        assertFalse(holder.state.value.isGoogleSubmitting)
        assertEquals(AuthFailure.Network, holder.state.value.authFailure)
    }

    @Test
    fun togglesPasswordVisibility() = runTest {
        val holder = holder()

        holder.onTogglePasswordVisibility()

        assertTrue(holder.state.value.isPasswordVisible)
    }
}
