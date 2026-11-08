package com.yallakhedma.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.presentation.theme.Yk
import kotlinx.coroutines.launch
import kotlin.math.abs

/** One tab of [YkBottomBar]. Icon-only; [label] is used for accessibility. */
data class YkNavItem(val icon: ImageVector, val label: String)

/** Height a screen should leave free at the bottom so content clears the bar. */
val LocalBottomBarInset = staticCompositionLocalOf { 0.dp }

/** Bar geometry (shared with hosts that need to pad their content). */
object YkBottomBarDefaults {
    val Height: Dp = 68.dp
    val SideMargin: Dp = 20.dp
    val BottomMargin: Dp = 12.dp
    /** Total vertical space the bar occupies above the system navigation bar. */
    val Inset: Dp = Height + BottomMargin + 12.dp
}

/**
 * Locked, pill-shaped bottom navigation.
 *
 *  - Tap a tab to switch.
 *  - Press-and-hold (or start sliding sideways) to scrub: a selector follows the
 *    finger in real time, the hovered tab previews with a haptic tick, and
 *    releasing commits to the tab under the finger.
 *  - The highlight glides between tabs with a soft bounce and grows while scrubbing.
 *
 * [onSelect] reports the new index; the bar never moves or floats.
 */
@Composable
fun YkBottomBar(
    items: List<YkNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = items.size
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentSelected by rememberUpdatedState(selectedIndex)

    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = YkBottomBarDefaults.SideMargin,
                end = YkBottomBarDefaults.SideMargin,
                bottom = YkBottomBarDefaults.BottomMargin,
            )
            .height(YkBottomBarDefaults.Height)
            .shadow(
                elevation = 24.dp,
                shape = shape,
                ambientColor = Yk.Night.copy(alpha = 0.5f),
                spotColor = Yk.Night.copy(alpha = 0.5f),
            )
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF24252C), Yk.Night)))
            .border(1.dp, Color.White.copy(alpha = 0.07f), shape),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(6.dp)) {
            val slotPx = with(density) { maxWidth.toPx() } / count
            val maxPx = slotPx * (count - 1)

            // Logical index ↔ physical slot (RTL lays tabs out right-to-left).
            fun slotOf(index: Int) = if (rtl) count - 1 - index else index
            fun indexAtX(x: Float): Int {
                val slot = (x / slotPx).toInt().coerceIn(0, count - 1)
                return if (rtl) count - 1 - slot else slot
            }

            var scrubbing by remember { mutableStateOf(false) }
            var hovered by remember { mutableIntStateOf(selectedIndex) }
            // Highlight position in physical px measured from the bar's left edge.
            val indicatorX = remember { Animatable(slotOf(selectedIndex) * slotPx) }

            // Glide to the committed tab with a slight bounce.
            LaunchedEffect(selectedIndex, scrubbing, slotPx) {
                if (!scrubbing) {
                    hovered = selectedIndex
                    indicatorX.animateTo(
                        targetValue = slotOf(selectedIndex) * slotPx,
                        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow),
                    )
                }
            }

            val grow by animateFloatAsState(
                targetValue = if (scrubbing) 1.08f else 1f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 600f),
                label = "indicator-grow",
            )

            // Highlight pill
            Box(
                modifier = Modifier
                    .width(maxWidth / count)
                    .fillMaxHeight()
                    .graphicsLayer {
                        // Physical position: always measured from the left edge.
                        translationX = if (rtl) -(maxPx - indicatorX.value) else indicatorX.value
                        scaleX = grow
                        scaleY = grow
                    }
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        Brush.verticalGradient(
                            listOf(Yk.Orange.copy(alpha = 0.30f), Yk.Orange.copy(alpha = 0.16f)),
                        ),
                    )
                    .border(1.dp, Yk.Orange.copy(alpha = 0.35f), RoundedCornerShape(percent = 50)),
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(count, rtl, slotPx) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val slop = viewConfiguration.touchSlop
                            var released = false

                            // Phase 1: tap, or hold / sideways slide → scrub.
                            val scrub = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                    if (change == null || !change.pressed) {
                                        released = true
                                        return@withTimeoutOrNull false
                                    }
                                    if (abs(change.position.x - down.position.x) > slop) {
                                        return@withTimeoutOrNull true
                                    }
                                }
                                @Suppress("UNREACHABLE_CODE") false
                            } ?: true // held past the long-press timeout

                            if (!scrub || released) {
                                val index = indexAtX(down.position.x)
                                if (index != currentSelected) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentOnSelect(index)
                                return@awaitEachGesture
                            }

                            // Phase 2: scrubbing — selector tracks the finger.
                            scrubbing = true
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            var lastX = down.position.x
                            fun follow(x: Float) {
                                lastX = x
                                // Center the pill under the finger (physical px from the left edge).
                                val physical = (x - slotPx / 2f).coerceIn(0f, maxPx)
                                scope.launch {
                                    indicatorX.animateTo(physical, spring(dampingRatio = 0.85f, stiffness = 1400f))
                                }
                                val h = indexAtX(x)
                                if (h != hovered) {
                                    hovered = h
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                            follow(down.position.x)
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                follow(change.position.x)
                                change.consume()
                            }
                            val committed = indexAtX(lastX)
                            scrubbing = false
                            currentOnSelect(committed)
                        }
                    },
            ) {
                items.forEachIndexed { index, item ->
                    val active = index == (if (scrubbing) hovered else selectedIndex)
                    NavIcon(
                        item = item,
                        active = active,
                        scrubbing = scrubbing,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics {
                                role = Role.Tab
                                selected = index == selectedIndex
                                contentDescription = item.label
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun NavIcon(item: YkNavItem, active: Boolean, scrubbing: Boolean, modifier: Modifier) {
    val tint by animateColorAsState(
        targetValue = if (active) Yk.Orange else Color.White.copy(alpha = 0.55f),
        animationSpec = tween(220),
        label = "nav-tint",
    )
    val scale by animateFloatAsState(
        targetValue = when {
            active && scrubbing -> 1.22f
            active -> 1.1f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "nav-scale",
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale },
        )
    }
}
