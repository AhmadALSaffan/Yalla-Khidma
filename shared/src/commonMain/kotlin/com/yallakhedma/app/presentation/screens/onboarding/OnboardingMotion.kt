package com.yallakhedma.app.presentation.screens.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.presentation.theme.Yk
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/*
 * Timeline helpers for the onboarding "motion videos".
 *
 * Every scene is a pure function of a timeline value t ∈ [0, 1] (driven by the
 * story progress bar), so each step plays like a short clip: elements enter on
 * cue, and scrubbing / swiping back replays it from the start.
 */

/** Local progress of a sub-clip that runs from [start] to [end] on the timeline. */
internal fun seg(t: Float, start: Float, end: Float): Float =
    ((t - start) / (end - start)).coerceIn(0f, 1f)

internal fun easeOutCubic(x: Float): Float {
    val p = 1f - x
    return 1f - p * p * p
}

/** Overshoots slightly then settles — for "pop" entrances. */
internal fun easeOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val p = x - 1f
    return 1f + c3 * p * p * p + c1 * p * p
}

/** +1 in LTR, -1 in RTL — multiply horizontal motion so it follows reading direction. */
@Composable
internal fun directionSign(): Float = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f

/**
 * Fade + rise (+ optional slide from the start side and scale) entrance.
 * [p] is the element's local progress (0 hidden → 1 settled).
 */
internal fun Modifier.reveal(
    p: Float,
    rise: Dp = 18.dp,
    slideX: Dp = 0.dp,
    dirSign: Float = 1f,
    scaleFrom: Float = 1f,
    pop: Boolean = false,
): Modifier = graphicsLayer {
    val eased = if (pop) easeOutBack(p) else easeOutCubic(p)
    alpha = p.coerceIn(0f, 1f)
    translationY = (1f - eased) * rise.toPx()
    translationX = -(1f - eased) * slideX.toPx() * dirSign
    val s = scaleFrom + (1f - scaleFrom) * eased
    scaleX = s
    scaleY = s
}

/**
 * Fixed-size design canvas ([designWidth]×[designHeight] dp) scaled to fit the
 * available space, so scenes look identical on every phone.
 */
@Composable
internal fun SceneStage(
    designWidth: Dp = 300.dp,
    designHeight: Dp = 320.dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val scale = min(maxWidth / designWidth, maxHeight / designHeight).coerceAtMost(1.25f)
        Box(
            modifier = Modifier
                .requiredSize(designWidth, designHeight)
                .graphicsLayer { scaleX = scale; scaleY = scale },
            content = content,
        )
    }
}

/** Slow-drifting soft blobs that give every scene ambient life. */
@Composable
internal fun BoxScope.AmbientBlobs(tint: Color = Yk.OrangeTint) {
    val drift = rememberInfiniteTransition(label = "blobs")
    val phase by drift.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart),
        label = "blob-phase",
    )
    val a = sin(phase * 2f * PI.toFloat())
    val b = sin((phase + 0.33f) * 2f * PI.toFloat())
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = (12 * a).dp, y = (-20 + 10 * b).dp)
            .size(170.dp)
            .background(Brush.radialGradient(listOf(tint.copy(alpha = 0.9f), Color.Transparent)), CircleShape),
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = (-24 + 10 * b).dp, y = (16 + 8 * a).dp)
            .size(200.dp)
            .background(Brush.radialGradient(listOf(tint.copy(alpha = 0.7f), Color.Transparent)), CircleShape),
    )
}
