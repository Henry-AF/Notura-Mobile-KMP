package com.notura.mobile.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.notura.mobile.ui.theme.NoturaTheme

/**
 * Outlined text field from the auth frames ("field-email", 2270:182): 1px border, 5px radius,
 * placeholder inside the field. [isError] switches the border to the error color; the design has
 * no error state, so that color comes from the web (see NoturaColors.error).
 */
@Composable
fun NoturaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    trailing: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = NoturaTheme.colors
    val spacing = NoturaTheme.spacing
    val textStyle = NoturaTheme.typography.field.copy(color = colors.title)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        visualTransformation = visualTransformation,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = placeholder
                if (isError) error(placeholder)
            },
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isError) colors.error else colors.fieldBorder, NoturaTheme.shapes.field)
                    .padding(horizontal = spacing.fieldHorizontalPadding, vertical = spacing.fieldVerticalPadding),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, style = textStyle)
                    innerTextField()
                }
                trailing?.invoke()
            }
        },
    )
}

/** Password variant with the eye toggle (2270:187). */
@Composable
fun NoturaPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
) {
    NoturaTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        isError = isError,
        enabled = enabled,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailing = {
            // Figma eye asset could not be exported (see PROGRESS.md); Material "Visibility" stands in.
            IconButton(
                onClick = onTogglePasswordVisibility,
                modifier = Modifier.width(NoturaTheme.spacing.fieldIconWidth).testTag(PASSWORD_TOGGLE_TAG),
            ) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = NoturaTheme.colors.fieldBorder,
                )
            }
        },
    )
}

const val PASSWORD_TOGGLE_TAG = "password-visibility-toggle"
