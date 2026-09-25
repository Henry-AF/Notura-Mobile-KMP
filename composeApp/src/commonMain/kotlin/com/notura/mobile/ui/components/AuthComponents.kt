package com.notura.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.notura.mobile.presentation.auth.AuthCopy
import com.notura.mobile.ui.theme.NoturaTheme

/**
 * "Notura" logo (2270:170). The waveform icon asset could not be exported from Figma
 * (see PROGRESS.md), so only the wordmark is rendered for now.
 */
@Composable
fun NoturaLogo(modifier: Modifier = Modifier) {
    Text(
        AuthCopy.BRAND,
        style = NoturaTheme.typography.titleLarge.copy(color = NoturaTheme.colors.brand),
        modifier = modifier,
    )
}

/** Back chevron (2270:164). Material's chevron stands in until the Figma asset is exported. */
@Composable
fun BackButton(onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier.testTag(BACK_BUTTON_TAG)) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = contentDescription,
            tint = NoturaTheme.colors.title,
        )
    }
}

/** Title and subtitle block (2270:178). */
@Composable
fun AuthHeader(title: String, subtitle: String, subtitleWidthModifier: Modifier, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NoturaTheme.spacing.titleToSubtitle),
        modifier = modifier,
    ) {
        Text(
            title,
            style = NoturaTheme.typography.titleLarge.copy(color = NoturaTheme.colors.title),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            subtitle,
            style = NoturaTheme.typography.subtitle.copy(color = NoturaTheme.colors.subtitle),
            textAlign = TextAlign.Center,
            modifier = subtitleWidthModifier,
        )
    }
}

/** "ou" divider (2270:193): 138dp lines at each side of the label. */
@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    val spacing = NoturaTheme.spacing
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(modifier = Modifier.width(spacing.dividerLineWidth), color = NoturaTheme.colors.fieldBorder)
        Text(AuthCopy.OR, style = NoturaTheme.typography.divider.copy(color = NoturaTheme.colors.subtitle))
        HorizontalDivider(modifier = Modifier.width(spacing.dividerLineWidth), color = NoturaTheme.colors.fieldBorder)
    }
}

/** Error line under a form, styled like the web (`text-sm text-destructive`). */
@Composable
fun AuthErrorText(message: String, modifier: Modifier = Modifier) {
    Text(
        message,
        style = NoturaTheme.typography.error.copy(color = NoturaTheme.colors.error),
        modifier = modifier.fillMaxWidth().testTag(AUTH_ERROR_TAG),
    )
}

/**
 * Terms row (2270:319): 15dp circle + "Ao se inscrever, você concorda com nossos Termos de Uso e
 * Privacidade." The whole row toggles acceptance. The checked look is not in Figma, so the circle is
 * filled with the primary color.
 */
@Composable
fun TermsCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, isError: Boolean, modifier: Modifier = Modifier) {
    val colors = NoturaTheme.colors
    val typography = NoturaTheme.typography
    val emphasis = SpanStyle(fontFamily = typography.emphasisFamily, fontWeight = FontWeight.SemiBold)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .testTag(TERMS_CHECKBOX_TAG),
    ) {
        Box(
            modifier = Modifier
                .size(NoturaTheme.spacing.checkboxSize)
                .border(1.dp, if (isError) colors.error else colors.fieldBorder, CircleShape)
                .background(if (checked) colors.primary else colors.background, CircleShape),
        )
        Text(
            buildAnnotatedString {
                append(AuthCopy.TERMS_PREFIX)
                withStyle(emphasis) { append(AuthCopy.TERMS) }
                append(AuthCopy.TERMS_JOIN)
                withStyle(emphasis) { append(AuthCopy.PRIVACY) }
                append(AuthCopy.TERMS_SUFFIX)
            },
            style = typography.terms.copy(color = colors.subtitle),
            modifier = Modifier.width(NoturaTheme.spacing.termsLabelWidth),
        )
    }
}

const val BACK_BUTTON_TAG = "auth-back"
const val AUTH_ERROR_TAG = "auth-error"
const val TERMS_CHECKBOX_TAG = "terms-checkbox"
