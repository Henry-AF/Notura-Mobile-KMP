package com.notura.mobile.ui.auth

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.semantics.SemanticsProperties
import com.notura.mobile.NoturaApp
import com.notura.mobile.SIGNED_IN_TAG
import com.notura.mobile.domain.auth.AuthFailure
import com.notura.mobile.domain.auth.AuthResult
import com.notura.mobile.domain.auth.AuthSessionState
import com.notura.mobile.domain.auth.SignUpOutcome
import com.notura.mobile.presentation.auth.AuthCopy
import com.notura.mobile.ui.components.AUTH_ERROR_TAG
import com.notura.mobile.ui.components.BACK_BUTTON_TAG
import com.notura.mobile.ui.components.TERMS_CHECKBOX_TAG
import com.notura.mobile.ui.theme.NoturaTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AuthFlowUiTest {

    private val hasError = SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)

    @Test
    fun introLeadsToSignInAndSignUp() = runComposeUiTest {
        val repository = FakeAuthRepository()
        setContent { NoturaTheme { AuthFlow(repository, googleSignIn = null) } }

        onNodeWithText(AuthCopy.INTRO_CTA).performClick()
        onNodeWithTag(SIGN_IN_SCREEN_TAG).assertIsDisplayed()
        onNodeWithText(AuthCopy.SIGN_IN_TITLE).assertIsDisplayed()

        onNodeWithTag(SIGN_UP_LINK_TAG).performClick()
        onNodeWithTag(SIGN_UP_SCREEN_TAG).assertIsDisplayed()
        onNodeWithText(AuthCopy.SIGN_UP_TITLE).assertIsDisplayed()

        onNodeWithTag(BACK_BUTTON_TAG).performClick()
        onNodeWithTag(SIGN_IN_SCREEN_TAG).assertIsDisplayed()
    }

    @Test
    fun signInShowsLoadingThenServerError() = runComposeUiTest {
        val repository = FakeAuthRepository()
        setContent { NoturaTheme { AuthFlow(repository, googleSignIn = null, startDestination = AuthDestination.SignIn) } }

        onNodeWithContentDescription(AuthCopy.EMAIL_PLACEHOLDER).performTextInput("ana@example.com")
        onNodeWithContentDescription(AuthCopy.PASSWORD_PLACEHOLDER).performTextInput("s3cret")
        onNodeWithText(AuthCopy.SIGN_IN_BUTTON).performClick()

        onNodeWithText(AuthCopy.SIGNING_IN).assertIsDisplayed()
        assertEquals(listOf("ana@example.com" to "s3cret"), repository.signInCalls)

        repository.nextSignIn.complete(AuthResult.Failure(AuthFailure.InvalidCredentials("Invalid login credentials")))
        waitForIdle()

        onNodeWithTag(AUTH_ERROR_TAG).assertIsDisplayed()
        onNodeWithText("Invalid login credentials").assertIsDisplayed()
        onNodeWithText(AuthCopy.SIGN_IN_BUTTON).assertIsDisplayed()
    }

    @Test
    fun signInWithEmptyFormMarksFieldsWithoutCallingRepository() = runComposeUiTest {
        val repository = FakeAuthRepository()
        setContent { NoturaTheme { AuthFlow(repository, googleSignIn = null, startDestination = AuthDestination.SignIn) } }

        onNodeWithText(AuthCopy.SIGN_IN_BUTTON).performClick()

        onNodeWithContentDescription(AuthCopy.EMAIL_PLACEHOLDER).assert(hasError)
        onNodeWithContentDescription(AuthCopy.PASSWORD_PLACEHOLDER).assert(hasError)
        assertTrue(repository.signInCalls.isEmpty())
    }

    @Test
    fun googleWithoutNativeLauncherShowsWebGoogleError() = runComposeUiTest {
        val repository = FakeAuthRepository()
        setContent { NoturaTheme { AuthFlow(repository, googleSignIn = null, startDestination = AuthDestination.SignIn) } }

        onNodeWithText(AuthCopy.GOOGLE).performClick()

        onNodeWithText(AuthCopy.GOOGLE_CONNECTION_ERROR).assertIsDisplayed()
    }

    @Test
    fun googleTokenIsExchangedThroughRepository() = runComposeUiTest {
        val repository = FakeAuthRepository()
        val launcher = GoogleSignInLauncher { GoogleIdTokenResult.Success("google-id-token", nonce = null) }
        setContent { NoturaTheme { AuthFlow(repository, googleSignIn = launcher, startDestination = AuthDestination.SignIn) } }

        onNodeWithText(AuthCopy.GOOGLE).performClick()

        onNodeWithText(AuthCopy.REDIRECTING_TO_GOOGLE).assertIsDisplayed()
        assertEquals(listOf("google-id-token"), repository.googleCalls)
    }

    @Test
    fun signUpRequiresTermsWithWebMessage() = runComposeUiTest {
        val repository = FakeAuthRepository()
        setContent { NoturaTheme { AuthFlow(repository, googleSignIn = null, startDestination = AuthDestination.SignUp) } }

        onNodeWithContentDescription(AuthCopy.NAME_PLACEHOLDER).performTextInput("Ana")
        onNodeWithContentDescription(AuthCopy.SIGN_UP_EMAIL_PLACEHOLDER).performTextInput("ana@example.com")
        onNodeWithContentDescription(AuthCopy.SIGN_UP_PASSWORD_PLACEHOLDER).performTextInput("s3cret-pass")
        onNodeWithText(AuthCopy.SIGN_UP_BUTTON).performClick()

        onNodeWithText(AuthCopy.TERMS_REQUIRED).assertIsDisplayed()
        assertTrue(repository.signUpCalls.isEmpty())
    }

    @Test
    fun signUpShowsLoadingThenConfirmationEmailSent() = runComposeUiTest {
        val repository = FakeAuthRepository()
        setContent { NoturaTheme { AuthFlow(repository, googleSignIn = null, startDestination = AuthDestination.SignUp) } }

        onNodeWithContentDescription(AuthCopy.NAME_PLACEHOLDER).performTextInput("Ana")
        onNodeWithContentDescription(AuthCopy.SIGN_UP_EMAIL_PLACEHOLDER).performTextInput("ana@example.com")
        onNodeWithContentDescription(AuthCopy.SIGN_UP_PASSWORD_PLACEHOLDER).performTextInput("s3cret-pass")
        onNodeWithTag(TERMS_CHECKBOX_TAG).performClick()
        onNodeWithText(AuthCopy.SIGN_UP_BUTTON).performClick()

        onNodeWithText(AuthCopy.SIGNING_UP).assertIsDisplayed()
        assertEquals(listOf(Triple("Ana", "ana@example.com", "s3cret-pass")), repository.signUpCalls)

        repository.nextSignUp.complete(AuthResult.Success(SignUpOutcome.ConfirmationEmailSent("ana@example.com")))
        waitForIdle()

        onNodeWithTag(CONFIRMATION_SENT_TAG).assertIsDisplayed()
        onNodeWithText("ana@example.com").assertIsDisplayed()

        onNodeWithText(AuthCopy.SIGN_IN_BUTTON).performClick()
        onNodeWithTag(SIGN_IN_SCREEN_TAG).assertIsDisplayed()
    }

    @Test
    fun signUpShowsServerError() = runComposeUiTest {
        val repository = FakeAuthRepository()
        setContent { NoturaTheme { AuthFlow(repository, googleSignIn = null, startDestination = AuthDestination.SignUp) } }

        onNodeWithContentDescription(AuthCopy.NAME_PLACEHOLDER).performTextInput("Ana")
        onNodeWithContentDescription(AuthCopy.SIGN_UP_EMAIL_PLACEHOLDER).performTextInput("ana@example.com")
        onNodeWithContentDescription(AuthCopy.SIGN_UP_PASSWORD_PLACEHOLDER).performTextInput("s3cret-pass")
        onNodeWithTag(TERMS_CHECKBOX_TAG).performClick()
        onNodeWithText(AuthCopy.SIGN_UP_BUTTON).performClick()
        repository.nextSignUp.complete(AuthResult.Failure(AuthFailure.EmailAlreadyRegistered("User already registered")))
        waitForIdle()

        onNodeWithText("User already registered").assertIsDisplayed()
    }

    @Test
    fun appShowsAuthWhenSignedOutAndLeavesItWhenSignedIn() = runComposeUiTest {
        val repository = FakeAuthRepository()
        setContent { NoturaApp(authRepository = repository, googleSignIn = null) }

        onNodeWithText(AuthCopy.INTRO_CTA).assertIsDisplayed()

        repository.sessionState.value = AuthSessionState.SignedIn("user-1", "ana@example.com")
        waitForIdle()

        onNodeWithTag(SIGNED_IN_TAG).assertExists()
    }
}
