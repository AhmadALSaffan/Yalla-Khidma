package com.yallakhedma.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.presentation.i18n.AppLanguage
import com.yallakhedma.app.presentation.i18n.LanguageManager
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.cd_back
import yallakhidma.shared.generated.resources.cd_change_language
import yallakhidma.shared.generated.resources.cd_logo
import yallakhidma.shared.generated.resources.icon

/** Circular icon button (48dp touch target by default). */
@Composable
fun YkIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Yk.Ink,
    background: Color = Color.Transparent,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    bordered: Boolean = false,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .then(if (bordered) Modifier.border(1.dp, Yk.Line, CircleShape) else Modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Top bar: back button (auto-mirrored for RTL), optional centered title, trailing actions. */
@Composable
fun YkTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(modifier = modifier.fillMaxWidth().height(56.dp)) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                YkIconButton(
                    icon = YkIcons.ArrowBack,
                    contentDescription = stringResource(Res.string.cd_back),
                    onClick = onBack,
                    bordered = true,
                    background = Yk.Canvas,
                )
            }
        }
        if (title != null) {
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 56.dp),
                style = MaterialTheme.typography.titleLarge,
                color = Yk.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = actions,
        )
    }
}

/** "EN" / "ع" pill that flips the whole app's language. */
@Composable
fun LanguageSwitch(
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
) {
    val manager = koinInject<LanguageManager>()
    val language by manager.language.collectAsState()
    val label = if (language == AppLanguage.Arabic) "EN" else "ع"
    val fg = if (onDark) Color.White else Yk.Ink
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(YkRadius.pill)
            .background(if (onDark) Color.White.copy(alpha = 0.16f) else Yk.Canvas)
            .border(1.dp, if (onDark) Color.White.copy(alpha = 0.3f) else Yk.Line, YkRadius.pill)
            .clickable(role = Role.Button, onClickLabel = stringResource(Res.string.cd_change_language)) {
                manager.toggle()
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(YkIcons.Globe, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

/** App mark (the launcher icon artwork) at any size. */
@Composable
fun BrandMark(size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.icon),
        contentDescription = stringResource(Res.string.cd_logo),
        modifier = modifier.size(size),
    )
}

/** Inline, animated error message box. Shown when [message] is non-null. */
@Composable
fun ErrorBanner(message: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = fadeIn(tween(180)) + expandVertically(),
        exit = fadeOut(tween(140)) + shrinkVertically(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(YkRadius.sm)
                .background(Yk.DangerSoft)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(Yk.Danger),
                contentAlignment = Alignment.Center,
            ) {
                Text("!", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
            Text(
                text = message.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = Yk.Danger,
            )
        }
    }
}

/** ─── label ─── divider. */
@Composable
fun LabeledDivider(label: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Yk.Line)
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Yk.Ink3,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Yk.Line)
    }
}

/** "Question? Action" footer row. */
@Composable
fun PromptLink(prompt: String, action: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(prompt, style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2)
        Spacer(Modifier.size(4.dp))
        Text(
            text = action,
            style = MaterialTheme.typography.titleSmall,
            color = Yk.Orange,
            modifier = Modifier
                .clip(YkRadius.chip)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 6.dp, vertical = 8.dp),
        )
    }
}
