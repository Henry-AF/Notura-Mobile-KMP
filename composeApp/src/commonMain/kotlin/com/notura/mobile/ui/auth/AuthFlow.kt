package com.notura.mobile.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.notura.mobile.data.auth.AuthRepository
import com.notura.mobile.presentation.auth.SignInStateHolder
import com.notura.mobile.presentation.auth.SignUpStateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class AuthDestination { Intro, SignIn, SignUp }

/**
 * Intro → sign in ⇄ sign up. Signing in is observed through AuthRepository.sessionState by the
 * app root, so this flow never navigates past auth itself.
 */
@Composable
fun AuthFlow(
    repository: AuthRepository,
    googleSignIn: GoogleSignInLauncher?,
    startDestination: AuthDestination = AuthDestination.Intro,
) {
    var destination by rememberSaveable { mutableStateOf(startDestination) }
    when (destination) {
        AuthDestination.Intro -> IntroScreen(onGetStarted = { destination = AuthDestination.SignIn })
        AuthDestination.SignIn -> SignInRoute(
            repository = repository,
            googleSignIn = googleSignIn,
            onSignUpClick = { destination = AuthDestination.SignUp },
            onBack = { destination = AuthDestination.Intro },
        )
        AuthDestination.SignUp -> SignUpRoute(
            repository = repository,
            googleSignIn = googleSignIn,
            onGoToSignIn = { destination = AuthDestination.SignIn },
            onBack = { destination = AuthDestination.SignIn },
        )
    }
}

@Composable
private fun SignInRoute(
    repository: AuthRepository,
    googleSignIn: GoogleSignInLauncher?,
    onSignUpClick: () -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val holder = remember(repository) { SignInStateHolder(repository, scope) }
    val state by holder.state.collectAsState()
    SignInScreen(
        state = state,
        onEmailChange = holder::onEmailChange,
        onPasswordChange = holder::onPasswordChange,
        onTogglePasswordVisibility = holder::onTogglePasswordVisibility,
        onSubmit = holder::onSubmit,
        onGoogleClick = {
            scope.requestGoogleIdToken(googleSignIn, onToken = holder::onGoogleIdToken, onError = holder::onGoogleSignInError)
        },
        onSignUpClick = onSignUpClick,
        onBack = onBack,
    )
}

@Composable
private fun SignUpRoute(
    repository: AuthRepository,
    googleSignIn: GoogleSignInLauncher?,
    onGoToSignIn: () -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val holder = remember(repository) { SignUpStateHolder(repository, scope) }
    val state by holder.state.collectAsState()
    val actions = remember(holder) {
        SignUpActions(
            onNameChange = holder::onNameChange,
            onEmailChange = holder::onEmailChange,
            onPasswordChange = holder::onPasswordChange,
            onTogglePasswordVisibility = holder::onTogglePasswordVisibility,
            onAcceptedTermsChange = holder::onAcceptedTermsChange,
            onSubmit = holder::onSubmit,
            onGoogleClick = {
                if (holder.canStartGoogleSignUp()) {
                    scope.requestGoogleIdToken(googleSignIn, onToken = holder::onGoogleIdToken, onError = holder::onGoogleSignInError)
                }
            },
            onGoToSignIn = onGoToSignIn,
            onBack = onBack,
        )
    }
    SignUpScreen(state = state, actions = actions)
}

/** A missing launcher means native Google sign-in is not wired on this platform yet: shown as a Google failure. */
private fun CoroutineScope.requestGoogleIdToken(
    launcher: GoogleSignInLauncher?,
    onToken: (idToken: String, nonce: String?) -> Unit,
    onError: () -> Unit,
) {
    if (launcher == null) {
        onError()
        return
    }
    launch {
        when (val result = launcher.requestIdToken()) {
            is GoogleIdTokenResult.Success -> onToken(result.idToken, result.nonce)
            GoogleIdTokenResult.Cancelled -> Unit
            GoogleIdTokenResult.Failed -> onError()
        }
    }
}
