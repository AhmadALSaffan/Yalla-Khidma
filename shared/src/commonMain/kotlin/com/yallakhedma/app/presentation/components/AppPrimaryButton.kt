package com.yallakhedma.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Legacy entry point — delegates to the design-system [YkButton]. */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    YkButton(text = text, onClick = onClick, modifier = modifier, enabled = enabled, loading = loading)
}
