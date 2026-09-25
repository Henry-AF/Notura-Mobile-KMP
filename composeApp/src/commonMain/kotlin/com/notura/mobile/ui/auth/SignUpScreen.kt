package com.notura.mobile.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.notura.mobile.presentation.auth.AuthCopy
import com.notura.mobile.presentation.auth.SignUpUiState
import com.notura.mobile.ui.components.AuthErrorText
import com.notura.mobile.ui.components.AuthHeader
import com.notura.mobile.ui.components.GoogleButton
import com.notura.mobile.ui.components.NoturaLogo
import com.notura.mobile.ui.components.NoturaPasswordField
import com.notura.mobile.ui.components.NoturaTextField
import com.notura.mobile.ui.components.OrDivider
import com.notura.mobile.ui.components.PrimaryButton
import com.notura.mobile.ui.components.TermsCheckbox
import com.notura.mobile.ui.theme.NoturaTheme

/** Callbacks of [SignUpScreen], grouped to keep the signature readable. */
class SignUpActions(
    val onNameChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onTogglePasswordVisibility: () -> Unit,
    val onAcceptedTermsChange: (Boolean) -> Unit,
    val onSubmit: () -> Unit,
    val onGoogleClick: () -> Unit,
    val onGoToSignIn: () -> Unit,
    val onBack: () -> Unit,
)

/** Figma "sign up" (2270:286). Stateless: state comes from SignUpStateHolder. */
@Composable
fun SignUpScreen(state: SignUpUiState, actions: SignUpActions, modifier: Modifier = Modifier) {
    val spacing = NoturaTheme.spacing
    AuthScaffold(onBack = actions.onBack, bottomPadding = spacing.signUpBottom, modifier = modifier.testTag(SIGN_UP_SCREEN_TAG)) {
        val sentTo = state.confirmationSentTo
        if (sentTo != null) {
            ConfirmationEmailSent(email = sentTo, onGoToSignIn = actions.onGoToSignIn)
        } else {
            SignUpForm(state, actions)
        }
    }
}

@Composable
private fun SignUpForm(state: SignUpUiState, actions: SignUpActions) {
    val spacing = NoturaTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.buttonGroupGap)) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.formToButton)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.signUpHeaderToFields),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.signUpLogoToTitle),
                ) {
                    NoturaLogo()
                    AuthHeader(
                        title = AuthCopy.SIGN_UP_TITLE,
                        subtitle = AuthCopy.SIGN_UP_SUBTITLE,
                        subtitleWidthModifier = Modifier.width(spacing.signUpSubtitleWidth),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(spacing.fieldsToTerms)) {
                    SignUpFields(state, actions)
                    TermsCheckbox(
                        checked = state.acceptedTerms,
                        onCheckedChange = actions.onAcceptedTermsChange,
                        isError = state.termsError != null,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.buttonGroupGap)) {
                SignUpErrors(state)
                PrimaryButton(
                    label = AuthCopy.SIGN_UP_BUTTON,
                    loadingLabel = AuthCopy.SIGNING_UP,
                    textStyle = NoturaTheme.typography.button,
                    isLoading = state.isSubmitting,
                    enabled = !state.isBusy,
                    onClick = actions.onSubmit,
                    modifier = Modifier.height(spacing.signUpButtonHeight),
                )
            }
        }
        OrDivider()
        GoogleButton(
            label = AuthCopy.GOOGLE,
            loadingLabel = AuthCopy.REDIRECTING_TO_GOOGLE,
            isLoading = state.isGoogleSubmitting,
            enabled = !state.isBusy,
            onClick = actions.onGoogleClick,
        )
    }
}

@Composable
private fun SignUpFields(state: SignUpUiState, actions: SignUpActions) {
    Column(verticalArrangement = Arrangement.spacedBy(NoturaTheme.spacing.fieldGap)) {
        NoturaTextField(
            value = state.name,
            onValueChange = actions.onNameChange,
            placeholder = AuthCopy.NAME_PLACEHOLDER,
            isError = state.nameError != null,
            enabled = !state.isBusy,
        )
        NoturaTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            placeholder = AuthCopy.SIGN_UP_EMAIL_PLACEHOLDER,
            isError = state.emailError != null,
            enabled = !state.isBusy,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        )
        NoturaPasswordField(
            value = state.password,
            onValueChange = actions.onPasswordChange,
            placeholder = AuthCopy.SIGN_UP_PASSWORD_PLACEHOLDER,
            isPasswordVisible = state.isPasswordVisible,
            onTogglePasswordVisibility = actions.onTogglePasswordVisibility,
            isError = state.passwordError != null,
            enabled = !state.isBusy,
        )
    }
}

/** Terms error takes precedence, as on the web, which shows a single error line. */
@Composable
private fun SignUpErrors(state: SignUpUiState) {
    val termsMessage = state.termsError?.let(AuthCopy::fieldErrorMessage)
    val message = termsMessage ?: state.authFailure?.let { AuthCopy.authFailureMessage(it, state.failedAction) }
    message?.let { AuthErrorText(it) }
}

/**
 * Shown when Supabase requires e-mail confirmation. There is no Figma frame or web copy for this
 * state (gap in PROGRESS.md), so it only shows the address and the existing "Sign In" action.
 */
@Composable
private fun ConfirmationEmailSent(email: String, onGoToSignIn: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NoturaTheme.spacing.formToButton),
        modifier = Modifier.testTag(CONFIRMATION_SENT_TAG),
    ) {
        NoturaLogo()
        Text(
            email,
            style = NoturaTheme.typography.subtitle.copy(color = NoturaTheme.colors.subtitle),
            textAlign = TextAlign.Center,
        )
        PrimaryButton(label = AuthCopy.SIGN_IN_BUTTON, onClick = onGoToSignIn)
    }
}

const val SIGN_UP_SCREEN_TAG = "sign-up-screen"
const val CONFIRMATION_SENT_TAG = "confirmation-email-sent"
