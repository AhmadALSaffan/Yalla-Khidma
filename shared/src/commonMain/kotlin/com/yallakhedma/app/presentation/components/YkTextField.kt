package com.yallakhedma.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius

/**
 * Text field with the label above the box (reads better in Arabic than a
 * floating label), a soft filled container, and an orange focus ring.
 *
 * [forceLtr] keeps email / password / numbers left-to-right even in Arabic.
 */
@Composable
fun YkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    forceLtr: Boolean = false,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    visualTransformation: VisualTransformation? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> Yk.Danger
            focused -> Yk.Orange
            else -> Yk.Line
        },
        animationSpec = tween(180),
        label = "field-border",
    )
    val borderWidth by animateDpAsState(if (focused || isError) 1.5.dp else 1.dp, tween(180), label = "field-bw")
    val container by animateColorAsState(
        targetValue = if (focused) Yk.Canvas else Yk.Fill,
        animationSpec = tween(180),
        label = "field-bg",
    )

    val baseStyle = MaterialTheme.typography.bodyLarge.copy(color = Yk.Ink)
    val textStyle = if (forceLtr) {
        baseStyle.copy(textDirection = TextDirection.Ltr, textAlign = TextAlign.Start)
    } else baseStyle

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = Yk.Ink2)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .background(container, YkRadius.field)
                .border(borderWidth, borderColor, YkRadius.field)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (focused) Yk.Orange else Yk.Ink3,
                    modifier = Modifier.size(20.dp),
                )
            }
            Box(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
                val field: @Composable () -> Unit = {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        singleLine = singleLine,
                        textStyle = textStyle,
                        cursorBrush = SolidColor(Yk.Orange),
                        interactionSource = interaction,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType,
                            imeAction = imeAction,
                            capitalization = capitalization,
                            autoCorrectEnabled = !forceLtr && !isPassword,
                        ),
                        keyboardActions = keyboardActions,
                        visualTransformation = when {
                            isPassword && !passwordVisible -> PasswordVisualTransformation()
                            visualTransformation != null -> visualTransformation
                            else -> VisualTransformation.None
                        },
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (value.isEmpty() && placeholder != null) {
                                    Text(
                                        text = placeholder,
                                        style = textStyle.copy(color = Yk.Ink3),
                                        maxLines = 1,
                                    )
                                }
                                inner()
                            }
                        },
                    )
                }
                if (forceLtr) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) { field() }
                } else field()
            }
            trailing?.invoke()
        }
        AnimatedVisibility(
            visible = supportingText != null,
            enter = fadeIn(tween(160)) + expandVertically(),
            exit = fadeOut(tween(120)) + shrinkVertically(),
        ) {
            Text(
                text = supportingText.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) Yk.Danger else Yk.Ink3,
            )
        }
    }
}

/** Eye toggle for password fields. */
@Composable
fun PasswordToggle(visible: Boolean, onToggle: () -> Unit, contentDescription: String) {
    YkIconButton(
        icon = if (visible) YkIcons.EyeOff else YkIcons.Eye,
        contentDescription = contentDescription,
        onClick = onToggle,
        tint = Yk.Ink3,
        size = 36.dp,
    )
}
