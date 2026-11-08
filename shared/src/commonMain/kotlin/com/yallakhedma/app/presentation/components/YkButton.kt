package com.yallakhedma.app.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.painterResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.ic_google

enum class YkButtonStyle { Primary, Dark, Outline, Soft, Ghost }

/**
 * The one button used across the app. 56dp, 16dp radius, springy press scale,
 * loading crossfade. Pick a [style] instead of hand-styling Material buttons.
 */
@Composable
fun YkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: YkButtonStyle = YkButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 56.dp,
    leading: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 900f),
        label = "yk-btn-scale",
    )
    val (container, content, border) = when (style) {
        YkButtonStyle.Primary -> Triple(Yk.Orange, Color.White, null)
        YkButtonStyle.Dark -> Triple(Yk.Night, Color.White, null)
        YkButtonStyle.Outline -> Triple(Yk.Canvas, Yk.Ink, BorderStroke(1.dp, Yk.LineStrong))
        YkButtonStyle.Soft -> Triple(Yk.OrangeSoft, Yk.OrangeDeep, null)
        YkButtonStyle.Ghost -> Triple(Color.Transparent, Yk.Orange, null)
    }
    val elevated = style == YkButtonStyle.Primary || style == YkButtonStyle.Dark
    val active = enabled && !loading

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (enabled) 1f else 0.4f }
            .then(
                if (elevated && enabled) {
                    Modifier.shadow(
                        elevation = if (pressed) 2.dp else 8.dp,
                        shape = YkRadius.button,
                        ambientColor = container.copy(alpha = 0.35f),
                        spotColor = container.copy(alpha = 0.35f),
                    )
                } else Modifier,
            )
            .clip(YkRadius.button)
            .background(container)
            .then(if (border != null) Modifier.border(border, YkRadius.button) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = content),
                enabled = active,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "yk-btn-content",
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = content,
                    strokeWidth = 2.5.dp,
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    leading?.invoke()
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        color = content,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Primary sign-in CTA — dark, with the official multicolor "G" mark. */
@Composable
fun GoogleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    YkButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = YkButtonStyle.Dark,
        loading = loading,
        enabled = enabled,
        leading = {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
    )
}
