package com.notura.mobile.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalNoturaColors = staticCompositionLocalOf { NoturaColors() }
private val LocalNoturaShapes = staticCompositionLocalOf { NoturaShapes() }
private val LocalNoturaSpacing = staticCompositionLocalOf { NoturaSpacing() }
private val LocalNoturaTypography = staticCompositionLocalOf<NoturaTypography> {
    error("NoturaTheme is missing: wrap the content in NoturaTheme { }")
}

/** Single source of the design tokens. Screens read values only through [NoturaTheme]. */
@Composable
fun NoturaTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalNoturaColors provides NoturaColors(),
        LocalNoturaShapes provides NoturaShapes(),
        LocalNoturaSpacing provides NoturaSpacing(),
        LocalNoturaTypography provides noturaTypography(),
        content = content,
    )
}

object NoturaTheme {
    val colors: NoturaColors
        @Composable @ReadOnlyComposable get() = LocalNoturaColors.current

    val shapes: NoturaShapes
        @Composable @ReadOnlyComposable get() = LocalNoturaShapes.current

    val spacing: NoturaSpacing
        @Composable @ReadOnlyComposable get() = LocalNoturaSpacing.current

    val typography: NoturaTypography
        @Composable @ReadOnlyComposable get() = LocalNoturaTypography.current
}
