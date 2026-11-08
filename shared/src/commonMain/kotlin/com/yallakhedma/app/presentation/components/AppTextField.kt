package com.yallakhedma.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Legacy entry point — delegates to the design-system [YkTextField].
 * Emails, passwords and numbers stay left-to-right; free text follows the
 * app language direction.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation? = null,
) {
    val ltr = isPassword || keyboardType in setOf(
        KeyboardType.Email, KeyboardType.Password, KeyboardType.Number,
        KeyboardType.Phone, KeyboardType.Decimal, KeyboardType.NumberPassword, KeyboardType.Uri,
    )
    YkTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        isError = isError,
        keyboardType = keyboardType,
        isPassword = isPassword,
        passwordVisible = passwordVisible,
        trailing = trailingIcon,
        singleLine = singleLine,
        forceLtr = ltr,
        visualTransformation = visualTransformation,
    )
}
