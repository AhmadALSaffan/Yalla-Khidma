package com.yallakhedma.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.cd_clear
import yallakhidma.shared.generated.resources.common_see_all
import yallakhidma.shared.generated.resources.distance_km
import yallakhidma.shared.generated.resources.distance_m
import yallakhidma.shared.generated.resources.rating_new

/** Soft card surface: white, hairline border, gentle shadow. */
fun Modifier.ykCard(shape: Shape = YkRadius.card, elevation: Dp = 10.dp): Modifier = this
    .shadow(elevation, shape, ambientColor = Yk.Ink.copy(alpha = 0.10f), spotColor = Yk.Ink.copy(alpha = 0.10f))
    .clip(shape)
    .background(Yk.Canvas)
    .border(1.dp, Yk.Line, shape)

/** Clickable with a subtle press-scale — makes cards feel tactile. */
fun Modifier.pressable(onClick: () -> Unit, enabled: Boolean = true): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.975f else 1f, tween(140), label = "press")
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
}

/** Moving highlight used by loading skeletons. */
fun Modifier.shimmer(shape: Shape = YkRadius.sm): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
        label = "shimmer-x",
    )
    this
        .clip(shape)
        .background(Yk.Fill)
        .drawWithContent {
            drawContent()
            val w = size.width
            drawRect(
                Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.7f), Color.Transparent),
                    start = Offset(w * x - w * 0.4f, 0f),
                    end = Offset(w * x + w * 0.4f, size.height),
                ),
            )
        }
}

/** Fade + lift entrance, staggered by [index]. */
@Composable
fun StaggeredItem(index: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val delay = (index * 50).coerceAtMost(400)
    val p by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(420, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "stagger",
    )
    Box(modifier.graphicsLayer { alpha = p; translationY = (1f - p) * 28.dp.toPx() }) { content() }
}

/** Remote image (http URL or Storage path) over a tinted placeholder. */
@Composable
fun RemoteImage(
    reference: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Color = Yk.OrangeSoft,
    fallback: @Composable () -> Unit = {},
) {
    val url = HomeImageAssets.imageUrl(reference)
    Box(modifier.background(placeholder), contentAlignment = Alignment.Center) {
        fallback()
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Circular avatar with an initial behind the (optional) photo. */
@Composable
fun Avatar(
    name: String,
    photo: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    ring: Color? = null,
    textSize: TextUnit = MaterialTheme.typography.titleMedium.fontSize,
) {
    RemoteImage(
        reference = photo,
        contentDescription = name,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(if (ring != null) Modifier.border(2.dp, ring, CircleShape) else Modifier),
        placeholder = Yk.OrangeTint,
    ) {
        val initial = name.trim().firstOrNull()?.uppercase()
        if (initial != null) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = textSize),
                color = Yk.OrangeDeep,
            )
        } else {
            Icon(YkIcons.UserFilled, contentDescription = null, tint = Yk.Orange.copy(alpha = 0.7f), modifier = Modifier.size(size * 0.5f))
        }
    }
}

/** ★ 4.8 pill; shows "New" when there's no rating yet. */
@Composable
fun RatingPill(rating: Double, modifier: Modifier = Modifier, onImage: Boolean = false) {
    Row(
        modifier = modifier
            .clip(YkRadius.pill)
            .background(if (onImage) Color.White else Yk.WarningSoft)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(YkIcons.StarFilled, null, tint = Yk.Warning, modifier = Modifier.size(12.dp))
        Text(
            text = if (rating > 0.0) formatRating(rating) else stringResource(Res.string.rating_new),
            style = MaterialTheme.typography.labelMedium,
            color = Yk.Ink,
        )
    }
}

fun formatRating(rating: Double): String {
    val rounded = (rating * 10).toInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

/** Localized "750 m" / "2.4 km". */
@Composable
fun distanceLabel(km: Double): String =
    if (km < 1.0) stringResource(Res.string.distance_m, (km * 1000).toInt())
    else stringResource(Res.string.distance_km, ((km * 10).toInt() / 10.0).toString())

/** Section title with an optional "See all" action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Yk.Ink, modifier = Modifier.weight(1f))
        if (onSeeAll != null) {
            Text(
                text = stringResource(Res.string.common_see_all),
                style = MaterialTheme.typography.titleSmall,
                color = Yk.Orange,
                modifier = Modifier
                    .clip(YkRadius.chip)
                    .clickable(role = Role.Button, onClick = onSeeAll)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            )
        }
    }
}

/** Selectable filter chip. */
@Composable
fun YkChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
) {
    val bg by animateColorAsState(if (selected) Yk.Night else Yk.Canvas, tween(180), label = "chip-bg")
    val fg by animateColorAsState(if (selected) Color.White else Yk.Ink2, tween(180), label = "chip-fg")
    val border by animateColorAsState(if (selected) Yk.Night else Yk.Line, tween(180), label = "chip-border")
    Row(
        modifier = modifier
            .clip(YkRadius.pill)
            .background(bg)
            .border(1.dp, border, YkRadius.pill)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (leading != null) Icon(leading, null, tint = fg, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1)
    }
}

/** Pill search field. Pass [onClick] to make it a read-only entry point instead. */
@Composable
fun YkSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    elevated: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .then(
                if (elevated) Modifier.shadow(12.dp, YkRadius.pill, ambientColor = Yk.Ink.copy(alpha = 0.12f), spotColor = Yk.Ink.copy(alpha = 0.12f))
                else Modifier,
            )
            .clip(YkRadius.pill)
            .background(if (elevated) Yk.Canvas else Yk.Fill)
            .border(1.dp, Yk.Line, YkRadius.pill)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(YkIcons.Search, null, tint = Yk.Orange, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = Yk.Ink3, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (onClick == null) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Yk.Ink),
                    cursorBrush = SolidColor(Yk.Orange),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
                )
            }
        }
        if (value.isNotEmpty() && onClick == null) {
            YkIconButton(
                icon = YkIcons.Close,
                contentDescription = stringResource(Res.string.cd_clear),
                onClick = { onValueChange("") },
                size = 32.dp,
                iconSize = 16.dp,
                tint = Yk.Ink2,
                background = Yk.Line,
            )
        }
    }
}

/** Friendly empty / error state with an icon tile and optional action. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(72.dp).clip(YkRadius.card).background(Yk.OrangeSoft),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Yk.Orange, modifier = Modifier.size(32.dp)) }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = Yk.Ink, textAlign = TextAlign.Center)
        if (body != null) {
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            YkButton(text = actionLabel, onClick = onAction, style = YkButtonStyle.Soft, height = 48.dp, modifier = Modifier.fillMaxWidth(0.7f))
        }
    }
}
