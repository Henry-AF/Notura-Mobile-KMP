package com.notura.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.notura.mobile.ui.theme.NoturaTheme

/**
 * Primary button ("btn-signin", 2270:191): filled, 7px radius, purple shadow. The Figma shadow
 * has five stacked layers; this uses the nearest one (0/10/21, 10%) as a single elevation.
 * While [isLoading], [loadingLabel] replaces [label] and the button ignores taps.
 */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = NoturaTheme.typography.buttonSmall,
    isLoading: Boolean = false,
    loadingLabel: String = label,
    enabled: Boolean = true,
) {
    val colors = NoturaTheme.colors
    val shape = NoturaTheme.shapes.button
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, shape = shape, ambientColor = colors.primaryShadow, spotColor = colors.primaryShadow)
            .clip(shape)
            .background(colors.primary)
            .clickable(enabled = enabled && !isLoading, role = Role.Button, onClick = onClick)
            .padding(vertical = NoturaTheme.spacing.buttonVerticalPadding),
    ) {
        Text(if (isLoading) loadingLabel else label, style = textStyle.copy(color = colors.onPrimary))
    }
}

/**
 * Google button ("field-account", 2270:197): outlined, 7px radius, soft grey shadow.
 * The Google logo asset could not be exported from Figma (see PROGRESS.md), so only the label shows.
 */
@Composable
fun GoogleButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    loadingLabel: String = label,
    enabled: Boolean = true,
) {
    val colors = NoturaTheme.colors
    val shape = NoturaTheme.shapes.button
    Row(
        horizontalArrangement = Arrangement.spacedBy(NoturaTheme.spacing.googleIconGap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = shape, ambientColor = colors.neutralShadow, spotColor = colors.neutralShadow)
            .clip(shape)
            .background(colors.background)
            .border(1.dp, colors.outlinedButtonBorder, shape)
            .clickable(enabled = enabled && !isLoading, role = Role.Button, onClick = onClick)
            .padding(vertical = NoturaTheme.spacing.buttonVerticalPadding),
    ) {
        Text(
            if (isLoading) loadingLabel else label,
            style = NoturaTheme.typography.buttonBold.copy(color = colors.subtitle),
        )
    }
}
