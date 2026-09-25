package com.notura.mobile.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.notura.mobile.presentation.auth.AuthCopy
import com.notura.mobile.presentation.auth.SignInUiState
import com.notura.mobile.ui.components.AuthErrorText
import com.notura.mobile.ui.components.AuthHeader
import com.notura.mobile.ui.components.GoogleButton
import com.notura.mobile.ui.components.NoturaLogo
import com.notura.mobile.ui.components.NoturaPasswordField
import com.notura.mobile.ui.components.NoturaTextField
import com.notura.mobile.ui.components.OrDivider
import com.notura.mobile.ui.components.PrimaryButton
import com.notura.mobile.ui.theme.NoturaTheme

/** Figma "sign in" (2270:160). Stateless: state comes from SignInStateHolder. */
@Composable
fun SignInScreen(
    state: SignInUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onGoogleClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = NoturaTheme.spacing
    val colors = NoturaTheme.colors
    val typography = NoturaTheme.typography
    AuthScaffold(onBack = onBack, bottomPadding = spacing.signInBottom, modifier = modifier.testTag(SIGN_IN_SCREEN_TAG)) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.buttonsToPrompt)) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.buttonGroupGap)) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.formToButton)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.signInHeaderToFields),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(spacing.signInLogoToTitle),
                            modifier = Modifier.width(spacing.signInHeaderWidth),
                        ) {
                            NoturaLogo()
                            AuthHeader(
                                title = AuthCopy.SIGN_IN_TITLE,
                                subtitle = AuthCopy.SIGN_IN_SUBTITLE,
                                subtitleWidthModifier = Modifier.fillMaxWidth(),
                            )
                        }
                        SignInFields(state, onEmailChange, onPasswordChange, onTogglePasswordVisibility)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.buttonGroupGap)) {
                        state.authFailure?.let { AuthErrorText(AuthCopy.authFailureMessage(it, state.failedAction)) }
                        PrimaryButton(
                            label = AuthCopy.SIGN_IN_BUTTON,
                            loadingLabel = AuthCopy.SIGNING_IN,
                            isLoading = state.isSubmitting,
                            enabled = !state.isBusy,
                            onClick = onSubmit,
                        )
                    }
                }
                OrDivider()
                GoogleButton(
                    label = AuthCopy.GOOGLE,
                    loadingLabel = AuthCopy.REDIRECTING_TO_GOOGLE,
                    isLoading = state.isGoogleSubmitting,
                    enabled = !state.isBusy,
                    onClick = onGoogleClick,
                )
            }
            Text(
                buildAnnotatedString {
                    append(AuthCopy.SIGN_UP_PROMPT)
                    withStyle(
                        SpanStyle(fontFamily = typography.emphasisFamily, fontWeight = FontWeight.SemiBold, color = colors.brand),
                    ) { append(AuthCopy.SIGN_UP_LINK) }
                },
                style = typography.footer.copy(color = colors.subtitle),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onSignUpClick).testTag(SIGN_UP_LINK_TAG),
            )
        }
        // Destinations for these links are not defined yet (PROGRESS.md), so they are plain text.
        Text(
            "${AuthCopy.TERMS}  ${AuthCopy.PRIVACY}",
            style = typography.footer.copy(color = colors.subtitle),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SignInFields(
    state: SignInUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
) {
    val spacing = NoturaTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.fieldGap)) {
        NoturaTextField(
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = AuthCopy.EMAIL_PLACEHOLDER,
            isError = state.emailError != null,
            enabled = !state.isBusy,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        )
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(spacing.passwordToForgot),
        ) {
            NoturaPasswordField(
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = AuthCopy.PASSWORD_PLACEHOLDER,
                isPasswordVisible = state.isPasswordVisible,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                isError = state.passwordError != null,
                enabled = !state.isBusy,
            )
            // No password-recovery screen in Figma yet (PROGRESS.md), so the link has no action.
            Text(
                AuthCopy.FORGOT_PASSWORD,
                style = NoturaTheme.typography.smallLink.copy(color = NoturaTheme.colors.mutedLink),
            )
        }
    }
}

const val SIGN_IN_SCREEN_TAG = "sign-in-screen"
const val SIGN_UP_LINK_TAG = "sign-up-link"
