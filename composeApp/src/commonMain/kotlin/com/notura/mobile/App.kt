package com.notura.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.notura.mobile.data.auth.AuthRepository
import com.notura.mobile.domain.auth.AuthSessionState
import com.notura.mobile.ui.auth.AuthFlow
import com.notura.mobile.ui.auth.GoogleSignInLauncher
import com.notura.mobile.ui.theme.NoturaTheme

/** App root: shows the auth flow while signed out. */
@Composable
fun NoturaApp(authRepository: AuthRepository, googleSignIn: GoogleSignInLauncher?) {
    NoturaTheme {
        val session by authRepository.sessionState.collectAsState()
        when (session) {
            AuthSessionState.SignedOut -> AuthFlow(repository = authRepository, googleSignIn = googleSignIn)
            // Restoring the stored session: background only, as there is no splash/loading frame in Figma.
            AuthSessionState.Loading -> EmptyScreen(LOADING_SESSION_TAG)
            // Home (step 3) is not built yet.
            is AuthSessionState.SignedIn -> EmptyScreen(SIGNED_IN_TAG)
        }
    }
}

@Composable
private fun EmptyScreen(tag: String) {
    Box(modifier = Modifier.fillMaxSize().background(NoturaTheme.colors.background).testTag(tag))
}

const val LOADING_SESSION_TAG = "loading-session"
const val SIGNED_IN_TAG = "signed-in"
