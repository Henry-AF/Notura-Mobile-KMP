package com.notura.mobile.presentation.auth

import com.notura.mobile.domain.auth.AuthFailure
import com.notura.mobile.domain.auth.AuthResult
import com.notura.mobile.domain.auth.SignUpOutcome
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
class SignUpStateHolderTest {

    private val repository = FakeAuthRepository()

    private fun TestScope.holder() = SignUpStateHolder(repository, backgroundScope)

    private fun SignUpStateHolder.fillValid() {
        onNameChange("Ana")
        onEmailChange("ana@example.com")
        onPasswordChange("s3cret-pass")
        onAcceptedTermsChange(true)
    }

    @Test
    fun submitGoesLoadingThenConfirmationEmailSent() = runTest {
        val holder = holder()
        holder.fillValid()

        holder.onSubmit()
        runCurrent()

        assertTrue(holder.state.value.isSubmitting)
        assertEquals(listOf(Triple("Ana", "ana@example.com", "s3cret-pass")), repository.signUpCalls)

        repository.nextSignUp.complete(AuthResult.Success(SignUpOutcome.ConfirmationEmailSent("ana@example.com")))
        runCurrent()

        assertFalse(holder.state.value.isSubmitting)
        assertEquals("ana@example.com", holder.state.value.confirmationSentTo)
    }

    @Test
    fun submitGoesLoadingThenSignedIn() = runTest {
        val holder = holder()
        holder.fillValid()

        holder.onSubmit()
        runCurrent()
        repository.nextSignUp.complete(AuthResult.Success(SignUpOutcome.SignedIn))
        runCurrent()

        assertFalse(holder.state.value.isSubmitting)
        assertEquals(null, holder.state.value.confirmationSentTo)
        assertEquals(null, holder.state.value.authFailure)
    }

    @Test
    fun submitGoesLoadingThenError() = runTest {
        val holder = holder()
        holder.fillValid()

        holder.onSubmit()
        runCurrent()
        repository.nextSignUp.complete(AuthResult.Failure(AuthFailure.EmailAlreadyRegistered("User already registered")))
        runCurrent()

        assertFalse(holder.state.value.isSubmitting)
        assertEquals(AuthFailure.EmailAlreadyRegistered("User already registered"), holder.state.value.authFailure)
        assertEquals(AuthAction.Email, holder.state.value.failedAction)
    }

    @Test
    fun validatesEveryFieldBeforeCallingRepository() = runTest {
        val holder = holder()
        holder.onPasswordChange("1234567")

        holder.onSubmit()
        runCurrent()

        val state = holder.state.value
        assertEquals(FieldError.NameRequired, state.nameError)
        assertEquals(FieldError.EmailRequired, state.emailError)
        assertEquals(FieldError.PasswordTooShort, state.passwordError)
        assertEquals(FieldError.TermsNotAccepted, state.termsError)
        assertTrue(repository.signUpCalls.isEmpty())
    }

    @Test
    fun passwordWithMinimumLengthIsAccepted() = runTest {
        val holder = holder()
        holder.fillValid()
        holder.onPasswordChange("12345678")

        holder.onSubmit()
        runCurrent()

        assertEquals(null, holder.state.value.passwordError)
        assertEquals(1, repository.signUpCalls.size)
    }

    @Test
    fun googleSignUpRequiresAcceptedTerms() = runTest {
        val holder = holder()

        assertFalse(holder.canStartGoogleSignUp())
        assertEquals(FieldError.TermsNotAcceptedForGoogle, holder.state.value.termsError)

        holder.onGoogleIdToken("id-token", null)
        runCurrent()
        assertTrue(repository.googleCalls.isEmpty())

        holder.onAcceptedTermsChange(true)
        assertTrue(holder.canStartGoogleSignUp())
        holder.onGoogleIdToken("id-token", null)
        runCurrent()

        assertTrue(holder.state.value.isGoogleSubmitting)
        assertEquals(1, repository.googleCalls.size)
    }

    @Test
    fun googleFlowErrorShowsGoogleFailure() = runTest {
        val holder = holder()

        holder.onGoogleSignInError()

        assertEquals(AuthFailure.Network, holder.state.value.authFailure)
        assertEquals(AuthAction.Google, holder.state.value.failedAction)
    }
}
