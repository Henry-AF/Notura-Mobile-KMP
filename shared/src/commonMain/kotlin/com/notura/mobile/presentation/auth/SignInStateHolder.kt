package com.notura.mobile.presentation.auth

import com.notura.mobile.data.auth.AuthRepository
import com.notura.mobile.domain.auth.AuthFailure
import com.notura.mobile.domain.auth.AuthResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val isSubmitting: Boolean = false,
    val isGoogleSubmitting: Boolean = false,
    val authFailure: AuthFailure? = null,
) {
    val isBusy: Boolean get() = isSubmitting || isGoogleSubmitting
}

/**
 * State for the "sign in" screen (Figma 2270:160). A successful sign-in is observed through
 * [AuthRepository.sessionState]; this holder only tracks the form and its errors.
 */
class SignInStateHolder(
    private val repository: AuthRepository,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = mutableState.asStateFlow()

    fun onEmailChange(email: String) =
        mutableState.update { it.copy(email = email, emailError = null, authFailure = null) }

    fun onPasswordChange(password: String) =
        mutableState.update { it.copy(password = password, passwordError = null, authFailure = null) }

    fun onTogglePasswordVisibility() =
        mutableState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onSubmit() {
        val current = mutableState.value
        if (current.isBusy) return
        val emailError = validateEmail(current.email)
        val passwordError = validateSignInPassword(current.password)
        if (emailError != null || passwordError != null) {
            mutableState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        mutableState.update { it.copy(isSubmitting = true, authFailure = null) }
        scope.launch {
            val result = repository.signIn(current.email, current.password)
            mutableState.update { it.copy(isSubmitting = false, authFailure = result.failureOrNull()) }
        }
    }

    /** Called once the platform Google sign-in sheet (Figma 2270:208) returns an ID token. */
    fun onGoogleIdToken(idToken: String, nonce: String?) {
        if (mutableState.value.isBusy) return
        mutableState.update { it.copy(isGoogleSubmitting = true, authFailure = null) }
        scope.launch {
            val result = repository.signInWithGoogleIdToken(idToken, nonce)
            mutableState.update { it.copy(isGoogleSubmitting = false, authFailure = result.failureOrNull()) }
        }
    }
}

internal fun AuthResult<*>.failureOrNull(): AuthFailure? = (this as? AuthResult.Failure)?.failure
