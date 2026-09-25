package com.notura.mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.notura.mobile.ui.components.BackButton
import com.notura.mobile.ui.theme.NoturaTheme

/**
 * Layout shared by the sign in and sign up frames ("main", 2270:163): back chevron at the top,
 * content spread to fill the screen, scrollable when the keyboard is open.
 */
@Composable
internal fun AuthScaffold(
    onBack: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = NoturaTheme.spacing
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(NoturaTheme.colors.background)
            .safeDrawingPadding()
            .imePadding(),
    ) {
        val minHeight = maxHeight
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = minHeight)
                .padding(
                    start = spacing.screenHorizontal,
                    end = spacing.screenHorizontal,
                    top = spacing.screenTop,
                    bottom = bottomPadding,
                ),
        ) {
            BackButton(onClick = onBack, contentDescription = "Voltar")
            content()
        }
    }
}
