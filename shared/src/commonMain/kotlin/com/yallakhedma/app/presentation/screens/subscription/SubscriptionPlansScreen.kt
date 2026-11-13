package com.yallakhedma.app.presentation.screens.subscription

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.PlanTier
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkButtonStyle
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.pressable
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.feat_ai
import yallakhidma.shared.generated.resources.feat_all_basic
import yallakhidma.shared.generated.resources.feat_all_pro
import yallakhidma.shared.generated.resources.feat_badge
import yallakhidma.shared.generated.resources.feat_boost
import yallakhidma.shared.generated.resources.feat_chat
import yallakhidma.shared.generated.resources.feat_gold
import yallakhidma.shared.generated.resources.feat_requests_n
import yallakhidma.shared.generated.resources.feat_search
import yallakhidma.shared.generated.resources.feat_stats
import yallakhidma.shared.generated.resources.feat_stats_adv
import yallakhidma.shared.generated.resources.feat_support
import yallakhidma.shared.generated.resources.feat_unlimited
import yallakhidma.shared.generated.resources.sub_cancel_note
import yallakhidma.shared.generated.resources.sub_current
import yallakhidma.shared.generated.resources.sub_monthly
import yallakhidma.shared.generated.resources.sub_per_month
import yallakhidma.shared.generated.resources.sub_per_year
import yallakhidma.shared.generated.resources.sub_popular
import yallakhidma.shared.generated.resources.sub_save
import yallakhidma.shared.generated.resources.sub_start
import yallakhidma.shared.generated.resources.sub_start_pro
import yallakhidma.shared.generated.resources.sub_subtitle
import yallakhidma.shared.generated.resources.sub_title
import yallakhidma.shared.generated.resources.sub_yearly

object SubscriptionPlansScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val current by rememberMySubscription()
        var yearly by rememberSaveable { mutableStateOf(false) }

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .background(Yk.WarmGlow)
                .safeTopPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.sub_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(YkIcons.Sparkles, null, tint = Yk.Orange, modifier = Modifier.size(16.dp))
                Text(stringResource(Res.string.sub_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.OrangeDeep)
            }

            Spacer(Modifier.height(20.dp))
            BillingToggle(yearly = yearly, onChange = { yearly = it })

            Spacer(Modifier.height(24.dp))
            val live = current?.takeIf { it.isLive }
            PlanTier.entries.forEach { tier ->
                PlanCard(
                    tier = tier,
                    yearly = yearly,
                    isCurrent = live?.plan == tier && live.yearly == yearly,
                    onChoose = { navigator.push(SubscriptionCheckoutScreen(tier.key, yearly)) },
                )
                Spacer(Modifier.height(16.dp))
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(YkIcons.ShieldCheck, null, tint = Yk.Ink3, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.sub_cancel_note), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink3)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BillingToggle(yearly: Boolean, onChange: (Boolean) -> Unit) {
    BoxWithConstraints(
        Modifier.fillMaxWidth().height(52.dp).clip(YkRadius.pill).background(Yk.Fill).border(1.dp, Yk.Line, YkRadius.pill).padding(4.dp),
    ) {
        val half = maxWidth / 2
        val offset by animateDpAsState(if (yearly) half else 0.dp, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow), label = "billing")
        Box(
            Modifier.offset(x = offset).width(half).fillMaxHeight()
                .shadow(6.dp, YkRadius.pill, ambientColor = Yk.Ink.copy(alpha = 0.1f), spotColor = Yk.Ink.copy(alpha = 0.1f))
                .clip(YkRadius.pill).background(Yk.Canvas),
        )
        Row(Modifier.fillMaxSize()) {
            listOf(false, true).forEach { option ->
                val selected = option == yearly
                val color by animateColorAsState(if (selected) Yk.Ink else Yk.Ink3, tween(180), label = "billing-text")
                Row(
                    Modifier.weight(1f).fillMaxHeight().clip(YkRadius.pill).clickable(role = Role.Tab) { onChange(option) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(if (option) Res.string.sub_yearly else Res.string.sub_monthly),
                        style = MaterialTheme.typography.titleSmall,
                        color = color,
                    )
                    if (option) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(Res.string.sub_save),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.clip(YkRadius.pill).background(Yk.Success).padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }
        }
    }
}

private data class Feature(val text: String, val included: Boolean = true)

@Composable
private fun features(tier: PlanTier): Pair<String?, List<Feature>> = when (tier) {
    PlanTier.Basic -> null to listOf(
        Feature(stringResource(Res.string.feat_search)),
        Feature(stringResource(Res.string.feat_requests_n, tier.monthlyRequests ?: 0)),
        Feature(stringResource(Res.string.feat_chat)),
        Feature(stringResource(Res.string.feat_badge), included = false),
        Feature(stringResource(Res.string.feat_stats_adv), included = false),
    )
    PlanTier.Pro -> stringResource(Res.string.feat_all_basic) to listOf(
        Feature(stringResource(Res.string.feat_boost)),
        Feature(stringResource(Res.string.feat_requests_n, tier.monthlyRequests ?: 0)),
        Feature(stringResource(Res.string.feat_badge)),
        Feature(stringResource(Res.string.feat_stats)),
    )
    PlanTier.Vip -> stringResource(Res.string.feat_all_pro) to listOf(
        Feature(stringResource(Res.string.feat_unlimited)),
        Feature(stringResource(Res.string.feat_ai)),
        Feature(stringResource(Res.string.feat_gold)),
        Feature(stringResource(Res.string.feat_support)),
    )
}

@Composable
private fun PlanCard(tier: PlanTier, yearly: Boolean, isCurrent: Boolean, onChoose: () -> Unit) {
    val highlighted = tier == PlanTier.Pro
    val dark = tier == PlanTier.Vip
    val fg = if (highlighted || dark) Color.White else Yk.Ink
    val sub = if (highlighted) Color.White.copy(alpha = 0.85f) else if (dark) Color.White.copy(alpha = 0.65f) else Yk.Ink3
    val bg: Brush = when {
        highlighted -> Yk.BrandGradient
        dark -> Brush.linearGradient(listOf(Color(0xFF2A2B33), Yk.Night))
        else -> Brush.linearGradient(listOf(Yk.Canvas, Yk.Canvas))
    }
    Box(Modifier.fillMaxWidth().padding(top = if (highlighted) 12.dp else 0.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(
                    if (highlighted) 20.dp else 6.dp, YkRadius.hero,
                    ambientColor = (if (highlighted) Yk.Orange else Yk.Ink).copy(alpha = 0.25f),
                    spotColor = (if (highlighted) Yk.Orange else Yk.Ink).copy(alpha = 0.25f),
                )
                .clip(YkRadius.hero)
                .background(bg)
                .then(if (!highlighted && !dark) Modifier.border(1.dp, if (isCurrent) Yk.Orange else Yk.Line, YkRadius.hero) else Modifier)
                .padding(22.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (dark) Icon(YkIcons.StarFilled, null, tint = Color(0xFFE9C46A), modifier = Modifier.size(18.dp))
                        Text(stringResource(tier.nameRes), style = MaterialTheme.typography.headlineSmall, color = fg)
                    }
                    Text(stringResource(tier.tagRes), style = MaterialTheme.typography.bodySmall, color = sub)
                }
                if (isCurrent) {
                    Text(
                        stringResource(Res.string.sub_current),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (highlighted || dark) Yk.OrangeDeep else Color.White,
                        modifier = Modifier.clip(YkRadius.pill).background(if (highlighted || dark) Color.White else Yk.Orange)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            AnimatedContent(
                yearly,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn(tween(200))) togetherWith (slideOutVertically { -it / 2 } + fadeOut(tween(120)))
                },
                label = "price",
            ) { y ->
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(usd(tier.price(y)), style = MaterialTheme.typography.displaySmall, color = fg)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(if (y) Res.string.sub_per_year else Res.string.sub_per_month),
                        style = MaterialTheme.typography.bodyMedium,
                        color = sub,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(if (highlighted || dark) Color.White.copy(alpha = 0.18f) else Yk.Line))
            Spacer(Modifier.height(14.dp))
            val (header, list) = features(tier)
            if (header != null) {
                Text(header, style = MaterialTheme.typography.labelLarge, color = sub)
                Spacer(Modifier.height(10.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                list.forEach { f ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val chipBg = when {
                            !f.included -> if (dark || highlighted) Color.White.copy(alpha = 0.1f) else Yk.Fill
                            highlighted -> Color.White.copy(alpha = 0.22f)
                            dark -> Color(0xFFE9C46A).copy(alpha = 0.18f)
                            else -> Yk.SuccessSoft
                        }
                        val chipFg = when {
                            !f.included -> sub
                            highlighted -> Color.White
                            dark -> Color(0xFFE9C46A)
                            else -> Yk.Success
                        }
                        Box(Modifier.size(22.dp).clip(CircleShape).background(chipBg), contentAlignment = Alignment.Center) {
                            Icon(if (f.included) YkIcons.Check else YkIcons.Close, null, tint = chipFg, modifier = Modifier.size(13.dp))
                        }
                        Text(f.text, style = MaterialTheme.typography.bodyMedium, color = if (f.included) fg else sub)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            when {
                highlighted -> Box(
                    Modifier.fillMaxWidth().height(52.dp).clip(YkRadius.button).background(Color.White).pressable(onChoose),
                    contentAlignment = Alignment.Center,
                ) { Text(stringResource(Res.string.sub_start_pro), style = MaterialTheme.typography.titleMedium, color = Yk.OrangeDeep) }
                dark -> Box(
                    Modifier.fillMaxWidth().height(52.dp).clip(YkRadius.button)
                        .background(Brush.linearGradient(listOf(Color(0xFFE9C46A), Color(0xFFC99A2E))))
                        .pressable(onChoose),
                    contentAlignment = Alignment.Center,
                ) { Text(stringResource(Res.string.sub_start), style = MaterialTheme.typography.titleMedium, color = Yk.Night) }
                else -> YkButton(
                    text = stringResource(Res.string.sub_start),
                    onClick = onChoose,
                    style = YkButtonStyle.Outline,
                    height = 52.dp,
                )
            }
        }
        if (highlighted) {
            Text(
                stringResource(Res.string.sub_popular),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
                    .clip(YkRadius.pill)
                    .background(Yk.Night)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}
