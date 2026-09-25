package com.notura.mobile.presentation.auth

import com.notura.mobile.data.auth.AuthRepository
import com.notura.mobile.domain.auth.AuthFailure
import com.notura.mobile.domain.auth.AuthResult
import com.notura.mobile.domain.auth.SignUpOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val acceptedTerms: Boolean = false,
    val nameError: FieldError? = null,
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val termsError: FieldError? = null,
    val isSubmitting: Boolean = false,
    val isGoogleSubmitting: Boolean = false,
    val authFailure: AuthFailure? = null,
    /** Set when Supabase requires e-mail confirmation before the first sign-in. */
    val confirmationSentTo: String? = null,
) {
    val isBusy: Boolean get() = isSubmitting || isGoogleSubmitting
}

/** State for the "sign up" screen (Figma 2270:286). */
class SignUpStateHolder(
    private val repository: AuthRepository,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = mutableState.asStateFlow()

    fun onNameChange(name: String) =
        mutableState.update { it.copy(name = name, nameError = null, authFailure = null) }

    fun onEmailChange(email: String) =
        mutableState.update { it.copy(email = email, emailError = null, authFailure = null) }

    fun onPasswordChange(password: String) =
        mutableState.update { it.copy(password = password, passwordError = null, authFailure = null) }

    fun onTogglePasswordVisibility() =
        mutableState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onAcceptedTermsChange(accepted: Boolean) =
        mutableState.update { it.copy(acceptedTerms = accepted, termsError = null) }

    fun onSubmit() {
        val current = mutableState.value
        if (current.isBusy) return
        val validated = current.withValidationErrors()
        if (validated.hasFieldErrors()) {
            mutableState.value = validated
            return
        }
        mutableState.update { it.copy(isSubmitting = true, authFailure = null) }
        scope.launch {
            val result = repository.signUp(current.name, current.email, current.password)
            mutableState.update { it.afterSignUp(result) }
        }
    }

    /**
     * The web requires accepting the terms before Google sign-up as well, so the ID token is only
     * exchanged when the checkbox is ticked. Returns false (and shows the error) otherwise.
     */
    fun canStartGoogleSignUp(): Boolean {
        if (mutableState.value.acceptedTerms) return true
        mutableState.update { it.copy(termsError = FieldError.TermsNotAccepted) }
        return false
    }

    fun onGoogleIdToken(idToken: String, nonce: String?) {
        val current = mutableState.value
        if (current.isBusy || !current.acceptedTerms) return
        mutableState.update { it.copy(isGoogleSubmitting = true, authFailure = null) }
        scope.launch {
            val result = repository.signInWithGoogleIdToken(idToken, nonce)
            mutableState.update { it.copy(isGoogleSubmitting = false, authFailure = result.failureOrNull()) }
        }
    }
}

private fun SignUpUiState.withValidationErrors(): SignUpUiState = copy(
    nameError = validateName(name),
    emailError = validateEmail(email),
    passwordError = validateNewPassword(password),
    termsError = if (acceptedTerms) null else FieldError.TermsNotAccepted,
)

private fun SignUpUiState.hasFieldErrors(): Boolean =
    nameError != null || emailError != null || passwordError != null || termsError != null

private fun SignUpUiState.afterSignUp(result: AuthResult<SignUpOutcome>): SignUpUiState = when (result) {
    is AuthResult.Failure -> copy(isSubmitting = false, authFailure = result.failure)
    is AuthResult.Success -> copy(
        isSubmitting = false,
        confirmationSentTo = (result.value as? SignUpOutcome.ConfirmationEmailSent)?.email,
    )
}
