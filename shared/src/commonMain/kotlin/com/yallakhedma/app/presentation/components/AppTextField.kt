package com.yallakhedma.app.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.text.KeyboardOptions

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
    // Force LTR text editing. LayoutDirection.Ltr alone isn't enough — the
    // OutlinedTextField's internal BasicTextField follows TextStyle.textDirection
    // for caret position and typed-character flow. We must override both.
    val ltrStyle = LocalTextStyle.current.copy(
        textDirection = TextDirection.Ltr,
        textAlign = TextAlign.Start,
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            singleLine = singleLine,
            textStyle = ltrStyle,
            shape = MaterialTheme.shapes.extraLarge,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = when {
                isPassword && !passwordVisible -> PasswordVisualTransformation()
                visualTransformation != null -> visualTransformation
                else -> VisualTransformation.None
            },
            trailingIcon = trailingIcon,
        )
    }
}
