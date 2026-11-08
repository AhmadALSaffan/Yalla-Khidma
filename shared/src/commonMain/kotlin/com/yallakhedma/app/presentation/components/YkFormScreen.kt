package com.yallakhedma.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.presentation.theme.Yk

/**
 * Standard layout for form-like screens: safe-area top bar, big title +
 * subtitle, scrollable content, and an optional sticky bottom action area.
 */
@Composable
fun YkFormScreen(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    background: Color = Yk.Canvas,
    topActions: @Composable RowScope.() -> Unit = {},
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(background)
            .safeTopPadding()
            .imePadding(),
    ) {
        YkTopBar(onBack = onBack, actions = topActions, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp))
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
            }
            Spacer(Modifier.height(24.dp))
            content()
            Spacer(Modifier.height(24.dp))
        }
        if (bottomBar != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, ambientColor = Yk.Ink.copy(alpha = 0.08f), spotColor = Yk.Ink.copy(alpha = 0.08f))
                    .background(Yk.Canvas),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    content = bottomBar,
                )
            }
        } else {
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
