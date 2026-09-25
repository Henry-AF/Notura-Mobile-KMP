package com.notura.mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.notura.mobile.presentation.auth.AuthCopy
import com.notura.mobile.ui.components.NoturaLogo
import com.notura.mobile.ui.components.PrimaryButton
import com.notura.mobile.ui.theme.NoturaTheme

/**
 * Figma "intro" (2270:148). Only the overview render of this frame could be read (Figma MCP
 * rate limit), and its hero illustration could not be exported: this version keeps the frame's
 * order (logo, hero area, title, body, CTA) with the shared tokens, pending the frame's detail.
 */
@Composable
fun IntroScreen(onGetStarted: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = NoturaTheme.spacing
    val colors = NoturaTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.fieldGap),
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenTop)
            .testTag(INTRO_SCREEN_TAG),
    ) {
        NoturaLogo(modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            AuthCopy.INTRO_TITLE,
            style = NoturaTheme.typography.titleLarge.copy(color = colors.title),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            AuthCopy.INTRO_BODY,
            style = NoturaTheme.typography.subtitle.copy(color = colors.subtitle),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        PrimaryButton(label = AuthCopy.INTRO_CTA, onClick = onGetStarted)
    }
}

const val INTRO_SCREEN_TAG = "intro-screen"
