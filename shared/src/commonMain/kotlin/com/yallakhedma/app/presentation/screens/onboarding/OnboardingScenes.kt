package com.yallakhedma.app.presentation.screens.onboarding

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.theme.Yk
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.scene_booking_title
import yallakhidma.shared.generated.resources.scene_cat_cleaning
import yallakhidma.shared.generated.resources.scene_cat_electric
import yallakhidma.shared.generated.resources.scene_cat_plumbing
import yallakhidma.shared.generated.resources.scene_chat_1
import yallakhidma.shared.generated.resources.scene_chat_2
import yallakhidma.shared.generated.resources.scene_distance
import yallakhidma.shared.generated.resources.scene_done
import yallakhidma.shared.generated.resources.scene_paid
import yallakhidma.shared.generated.resources.scene_provider_name
import yallakhidma.shared.generated.resources.scene_provider_role
import yallakhidma.shared.generated.resources.scene_result_1
import yallakhidma.shared.generated.resources.scene_result_2
import yallakhidma.shared.generated.resources.scene_result_3
import yallakhidma.shared.generated.resources.scene_reviews
import yallakhidma.shared.generated.resources.scene_search_query
import yallakhidma.shared.generated.resources.scene_step_done
import yallakhidma.shared.generated.resources.scene_step_on_way
import yallakhidma.shared.generated.resources.scene_step_requested
import yallakhidma.shared.generated.resources.scene_verified
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val CardShape = RoundedCornerShape(18.dp)

/** White elevated card used by all scenes. */
@Composable
private fun SceneCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .shadow(14.dp, CardShape, ambientColor = Yk.Orange.copy(alpha = 0.18f), spotColor = Yk.Ink.copy(alpha = 0.12f))
            .clip(CardShape)
            .background(Yk.Canvas),
    ) { content() }
}

@Composable
private fun Avatar(initial: String, size: Dp, colors: List<Color>) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun Stars(filled: Float, size: Dp = 12.dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { i ->
            val p = (filled * 5f - i).coerceIn(0f, 1f)
            Icon(
                imageVector = YkIcons.StarFilled,
                contentDescription = null,
                tint = lerp(Yk.Line, Yk.Warning, p),
                modifier = Modifier.size(size).graphicsLayer {
                    val s = 0.7f + 0.3f * easeOutBack(p)
                    scaleX = s; scaleY = s
                },
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Scene 1 — Search: type a query, pick a category, results stream in.
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun SearchScene(t: Float) {
    val dir = directionSign()
    val blink = rememberInfiniteTransition(label = "caret")
    val caret by blink.animateFloat(1f, 0f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "caret-a")

    SceneStage {
        AmbientBlobs()

        // Search bar + typing
        val query = stringResource(Res.string.scene_search_query)
        val typed = (seg(t, 0.10f, 0.30f) * query.length).toInt().coerceIn(0, query.length)
        SceneCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 20.dp)
                .width(264.dp)
                .height(54.dp)
                .reveal(seg(t, 0f, 0.12f), rise = (-18).dp, scaleFrom = 0.92f),
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(YkIcons.Search, null, tint = Yk.Orange, modifier = Modifier.size(20.dp))
                Text(query.take(typed), style = MaterialTheme.typography.titleMedium, color = Yk.Ink)
                Box(
                    Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .graphicsLayer { alpha = if (t < 0.34f) caret else 0f }
                        .background(Yk.Orange),
                )
            }
        }

        // Category chips — pop in, first one becomes selected
        val chips = listOf(
            stringResource(Res.string.scene_cat_plumbing),
            stringResource(Res.string.scene_cat_electric),
            stringResource(Res.string.scene_cat_cleaning),
        )
        val selected = seg(t, 0.44f, 0.50f)
        Row(
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chips.forEachIndexed { i, label ->
                val p = seg(t, 0.28f + i * 0.05f, 0.40f + i * 0.05f)
                val on = if (i == 0) selected else 0f
                Box(
                    modifier = Modifier
                        .reveal(p, rise = 10.dp, scaleFrom = 0.6f, pop = true)
                        .clip(RoundedCornerShape(50))
                        .background(lerp(Yk.Canvas, Yk.Orange, on))
                        .border(1.dp, lerp(Yk.Line, Yk.Orange, on), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = lerp(Yk.Ink2, Color.White, on))
                }
            }
        }

        // Results stream in
        val names = listOf(
            stringResource(Res.string.scene_result_1),
            stringResource(Res.string.scene_result_2),
            stringResource(Res.string.scene_result_3),
        )
        val km = listOf("1.2", "2.4", "3.1")
        val palettes = listOf(
            listOf(Color(0xFFFF9A62), Yk.Orange),
            listOf(Color(0xFF7DD3FC), Color(0xFF3B82F6)),
            listOf(Color(0xFF6EE7B7), Yk.Success),
        )
        Column(
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 142.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            names.forEachIndexed { i, name ->
                val p = seg(t, 0.50f + i * 0.09f, 0.66f + i * 0.09f)
                SceneCard(
                    modifier = Modifier
                        .width(264.dp)
                        .height(52.dp)
                        .reveal(p, rise = 26.dp, slideX = 14.dp, dirSign = dir),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Avatar(name.take(1), 32.dp, palettes[i])
                        Column(Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.labelLarge, color = Yk.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Stars(filled = seg(t, 0.60f + i * 0.09f, 0.80f + i * 0.09f), size = 10.dp)
                        }
                        Text(
                            stringResource(Res.string.scene_distance, km[i]),
                            style = MaterialTheme.typography.labelMedium,
                            color = Yk.Ink3,
                        )
                    }
                }
            }
        }

        // Tap ripple on the top result
        val tap = seg(t, 0.86f, 1f)
        if (tap > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    // offset() is already direction-aware: negative x = toward the start side.
                    .offset(y = 150.dp, x = (-92).dp)
                    .size(36.dp)
                    .graphicsLayer {
                        val s = 0.4f + 1.6f * easeOutCubic(tap)
                        scaleX = s; scaleY = s
                        alpha = (1f - tap) * 0.9f
                    }
                    .background(Yk.Orange.copy(alpha = 0.35f), CircleShape),
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Scene 2 — Trust: verified profile, rating fills, a quick chat.
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun TrustScene(t: Float) {
    val dir = directionSign()
    val pulseT = rememberInfiniteTransition(label = "ring")
    val ring by pulseT.animateFloat(0f, 1f, infiniteRepeatable(tween(1600)), label = "ring-p")

    SceneStage {
        AmbientBlobs()

        val name = stringResource(Res.string.scene_provider_name)
        SceneCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 16.dp)
                .width(264.dp)
                .height(156.dp)
                .reveal(seg(t, 0f, 0.18f), rise = 24.dp, scaleFrom = 0.9f),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        val verified = seg(t, 0.28f, 0.40f)
                        if (verified >= 1f) {
                            Box(
                                Modifier
                                    .size(56.dp)
                                    .graphicsLayer { val s = 1f + 0.45f * ring; scaleX = s; scaleY = s; alpha = 1f - ring }
                                    .border(2.dp, Yk.Success, CircleShape),
                            )
                        }
                        Avatar(name.take(1), 56.dp, listOf(Color(0xFFFFB38A), Yk.Orange))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(name, style = MaterialTheme.typography.titleMedium, color = Yk.Ink)
                        Text(stringResource(Res.string.scene_provider_role), style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
                    }
                }
                // Verified badge pops
                Row(
                    modifier = Modifier
                        .reveal(seg(t, 0.28f, 0.42f), rise = 0.dp, scaleFrom = 0.4f, pop = true)
                        .clip(RoundedCornerShape(50))
                        .background(Yk.SuccessSoft)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(YkIcons.ShieldCheck, null, tint = Yk.Success, modifier = Modifier.size(14.dp))
                    Text(stringResource(Res.string.scene_verified), style = MaterialTheme.typography.labelMedium, color = Yk.Success)
                }
                // Rating fills + review count counts up
                val rating = seg(t, 0.42f, 0.62f)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Stars(filled = rating, size = 14.dp)
                    Text(
                        text = "4.9",
                        style = MaterialTheme.typography.labelLarge,
                        color = Yk.Ink,
                        modifier = Modifier.graphicsLayer { alpha = rating },
                    )
                    Text(
                        text = stringResource(Res.string.scene_reviews, (128 * easeOutCubic(rating)).toInt()),
                        style = MaterialTheme.typography.labelMedium,
                        color = Yk.Ink3,
                    )
                }
            }
        }

        // Chat: client asks (end side, orange) → typing → provider replies (start side)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 190.dp)
                .width(264.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.fillMaxWidth()) {
                Bubble(
                    text = stringResource(Res.string.scene_chat_1),
                    mine = true,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .reveal(seg(t, 0.62f, 0.74f), rise = 12.dp, slideX = (-16).dp, dirSign = dir, scaleFrom = 0.85f),
                )
            }
            Box(Modifier.fillMaxWidth().height(40.dp)) {
                val typing = seg(t, 0.74f, 0.80f)
                val reply = seg(t, 0.82f, 0.94f)
                if (reply <= 0f && typing > 0f) {
                    TypingDots(Modifier.align(Alignment.CenterStart).reveal(typing, rise = 6.dp))
                }
                Bubble(
                    text = stringResource(Res.string.scene_chat_2),
                    mine = false,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .reveal(reply, rise = 12.dp, slideX = 16.dp, dirSign = dir, scaleFrom = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun Bubble(text: String, mine: Boolean, modifier: Modifier = Modifier) {
    val shape = if (mine) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }
    Box(
        modifier = modifier
            .shadow(6.dp, shape, ambientColor = Yk.Ink.copy(alpha = 0.08f), spotColor = Yk.Ink.copy(alpha = 0.08f))
            .clip(shape)
            .background(if (mine) Yk.Orange else Yk.Canvas)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = if (mine) Color.White else Yk.Ink)
    }
}

@Composable
private fun TypingDots(modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "typing")
    val phase by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(900)), label = "typing-p")
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Yk.Canvas)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(3) { i ->
            val local = ((phase * 3f - i) % 3f).let { if (it < 0) it + 3f else it }
            val lift = if (local < 1f) sin(local * PI.toFloat()) else 0f
            Box(
                Modifier
                    .size(6.dp)
                    .graphicsLayer { translationY = -lift * 4.dp.toPx() }
                    .background(Yk.Ink3, CircleShape),
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Scene 3 — Done: tracker completes, success check draws, confetti bursts.
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun DoneScene(t: Float) {
    SceneStage {
        AmbientBlobs(tint = Color(0xFFDDF6EC))

        // Booking tracker card
        SceneCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 12.dp)
                .width(264.dp)
                .height(108.dp)
                .reveal(seg(t, 0f, 0.16f), rise = 24.dp, scaleFrom = 0.92f),
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Yk.OrangeSoft),
                        contentAlignment = Alignment.Center,
                    ) { Icon(YkIcons.Wrench, null, tint = Yk.Orange, modifier = Modifier.size(16.dp)) }
                    Text(stringResource(Res.string.scene_booking_title), style = MaterialTheme.typography.titleSmall, color = Yk.Ink)
                }
                Tracker(
                    line1 = seg(t, 0.16f, 0.34f),
                    line2 = seg(t, 0.36f, 0.54f),
                    labels = listOf(
                        stringResource(Res.string.scene_step_requested),
                        stringResource(Res.string.scene_step_on_way),
                        stringResource(Res.string.scene_step_done),
                    ),
                )
            }
        }

        // Success badge: ring sweep → check draws → confetti
        val badge = seg(t, 0.54f, 0.66f)
        val ring = seg(t, 0.54f, 0.72f)
        val check = seg(t, 0.64f, 0.78f)
        val confetti = seg(t, 0.66f, 1f)
        Box(
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 140.dp).size(150.dp),
            contentAlignment = Alignment.Center,
        ) {
            Confetti(confetti)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        val s = 0.3f + 0.7f * easeOutBack(badge)
                        scaleX = s; scaleY = s; alpha = badge
                    },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 6.dp.toPx()
                    drawCircle(Yk.SuccessSoft)
                    drawArc(
                        color = Yk.Success,
                        startAngle = -90f,
                        sweepAngle = 360f * easeOutCubic(ring),
                        useCenter = false,
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                    val path = Path().apply {
                        moveTo(size.width * 0.30f, size.height * 0.52f)
                        lineTo(size.width * 0.44f, size.height * 0.66f)
                        lineTo(size.width * 0.71f, size.height * 0.38f)
                    }
                    val measure = PathMeasure().apply { setPath(path, false) }
                    val partial = Path()
                    measure.getSegment(0f, measure.length * easeOutCubic(check), partial, true)
                    drawPath(partial, Yk.Success, style = Stroke(7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 266.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(Res.string.scene_done),
                style = MaterialTheme.typography.titleMedium,
                color = Yk.Ink,
                modifier = Modifier.reveal(seg(t, 0.72f, 0.84f), rise = 10.dp),
            )
            Row(
                modifier = Modifier
                    .reveal(seg(t, 0.80f, 0.92f), rise = 0.dp, scaleFrom = 0.5f, pop = true)
                    .clip(RoundedCornerShape(50))
                    .background(Yk.Night)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(YkIcons.Lock, null, tint = Color.White, modifier = Modifier.size(12.dp))
                Text(stringResource(Res.string.scene_paid), style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
        }
    }
}

@Composable
private fun Tracker(line1: Float, line2: Float, labels: List<String>) {
    val dots = listOf(1f, if (line1 >= 1f) 1f else 0f, if (line2 >= 1f) 1f else 0f)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TrackerDot(dots[0])
            TrackerLine(line1, Modifier.weight(1f))
            TrackerDot(dots[1])
            TrackerLine(line2, Modifier.weight(1f))
            TrackerDot(dots[2], done = true)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEachIndexed { i, label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (dots[i] >= 1f) Yk.Ink else Yk.Ink3,
                )
            }
        }
    }
}

@Composable
private fun TrackerDot(on: Float, done: Boolean = false) {
    val color = lerp(Yk.Line, if (done) Yk.Success else Yk.Orange, on)
    Box(
        Modifier
            .size(14.dp)
            .graphicsLayer { val s = 1f + 0.25f * on; scaleX = s; scaleY = s }
            .background(color, CircleShape),
    )
}

@Composable
private fun TrackerLine(p: Float, modifier: Modifier) {
    Box(modifier.height(3.dp).padding(horizontal = 4.dp).clip(RoundedCornerShape(50)).background(Yk.Line)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(p).background(Yk.Orange))
    }
}

@Composable
private fun Confetti(p: Float) {
    if (p <= 0f) return
    val pieces = remember {
        List(22) { i ->
            val angle = (i / 22f) * 2f * PI.toFloat() + (i % 3) * 0.17f
            val dist = 0.55f + (i * 37 % 10) / 22f
            val color = listOf(Yk.Orange, Yk.Success, Yk.Warning, Yk.Info, Color(0xFFFFB38A))[i % 5]
            Triple(angle, dist, color)
        }
    }
    Canvas(Modifier.fillMaxSize()) {
        val e = easeOutCubic(p)
        val radius = size.minDimension * 0.62f
        pieces.forEachIndexed { i, (angle, dist, color) ->
            val x = center.x + cos(angle) * radius * dist * e
            val y = center.y + sin(angle) * radius * dist * e + p * p * 36.dp.toPx()
            val w = if (i % 2 == 0) 8.dp.toPx() else 6.dp.toPx()
            rotate(degrees = p * 540f + i * 23f, pivot = Offset(x, y)) {
                drawRoundRect(
                    color = color.copy(alpha = (1f - p).coerceIn(0f, 1f)),
                    topLeft = Offset(x - w / 2, y - w / 4),
                    size = Size(w, w / 2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                )
            }
        }
    }
}
