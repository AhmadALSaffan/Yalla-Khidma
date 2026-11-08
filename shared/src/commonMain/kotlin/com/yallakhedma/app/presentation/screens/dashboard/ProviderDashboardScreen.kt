package com.yallakhedma.app.presentation.screens.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.LocalBottomBarInset
import com.yallakhedma.app.presentation.components.SectionHeader
import com.yallakhedma.app.presentation.components.YkIconButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.formatRating
import com.yallakhedma.app.presentation.components.pressable
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.i18n.AppLanguage
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.screens.common.ProviderRequestCard
import com.yallakhedma.app.presentation.screens.common.formatMoney
import com.yallakhedma.app.presentation.screens.coupons.ProviderCouponsScreen
import com.yallakhedma.app.presentation.screens.main.LocalTabController
import com.yallakhedma.app.presentation.screens.main.ProviderMainScreen
import com.yallakhedma.app.presentation.screens.services.AddServiceScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.cd_notifications
import yallakhidma.shared.generated.resources.currency_sar
import yallakhidma.shared.generated.resources.dash_action_add_service
import yallakhidma.shared.generated.resources.dash_action_coupons
import yallakhidma.shared.generated.resources.dash_action_requests
import yallakhidma.shared.generated.resources.dash_action_support
import yallakhidma.shared.generated.resources.dash_hello
import yallakhidma.shared.generated.resources.dash_online
import yallakhidma.shared.generated.resources.dash_pending_empty
import yallakhidma.shared.generated.resources.dash_pending_title
import yallakhidma.shared.generated.resources.dash_provider_fallback
import yallakhidma.shared.generated.resources.dash_quick_title
import yallakhidma.shared.generated.resources.dash_stat_bookings
import yallakhidma.shared.generated.resources.dash_stat_earnings
import yallakhidma.shared.generated.resources.dash_stat_pending
import yallakhidma.shared.generated.resources.dash_stat_rating
import yallakhidma.shared.generated.resources.dash_week_title
import yallakhidma.shared.generated.resources.dash_week_total
import yallakhidma.shared.generated.resources.profile_soon
import yallakhidma.shared.generated.resources.rating_new

/** Provider home (tab content of [ProviderMainScreen]). */
object ProviderDashboardScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val tabs = LocalTabController.current
        val model = koinScreenModel<ProviderDashboardScreenModel>()
        val state by model.state.collectAsState()
        val provider = state.provider
        val name = provider?.name?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.dash_provider_fallback)
        val openRequests: () -> Unit = { tabs?.select(ProviderMainScreen.TAB_REQUESTS) }

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Fill)
                .verticalScroll(rememberScrollState())
                .safeTopPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = LocalBottomBarInset.current + 16.dp),
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Avatar(name, provider?.photoUrl, 52.dp, modifier = Modifier.pressable({ tabs?.select(ProviderMainScreen.TAB_PROFILE) }))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.dash_hello), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink3)
                    Text(name, style = MaterialTheme.typography.headlineMedium, color = Yk.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                YkIconButton(
                    icon = YkIcons.Bell,
                    contentDescription = stringResource(Res.string.cd_notifications),
                    onClick = openRequests,
                    bordered = true,
                    background = Yk.Canvas,
                )
            }
            Row(
                Modifier
                    .padding(top = 12.dp)
                    .clip(YkRadius.pill)
                    .background(Yk.SuccessSoft)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Yk.Success))
                Text(stringResource(Res.string.dash_online), style = MaterialTheme.typography.labelLarge, color = Yk.Success)
            }

            // Stats
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    YkIcons.Clipboard, Yk.Orange, stringResource(Res.string.dash_stat_pending),
                    state.pending.size.toString(), Modifier.weight(1f), highlight = state.pending.isNotEmpty(),
                )
                StatTile(
                    YkIcons.Wallet, Yk.Success, stringResource(Res.string.dash_stat_earnings),
                    "${formatMoney(state.monthEarnings)} ${stringResource(Res.string.currency_sar)}", Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val rating = provider?.rating ?: 0.0
                StatTile(
                    YkIcons.StarFilled, Yk.Warning, stringResource(Res.string.dash_stat_rating),
                    if (rating > 0) formatRating(rating) else stringResource(Res.string.rating_new), Modifier.weight(1f),
                )
                StatTile(
                    YkIcons.CalendarCheck, Yk.Info, stringResource(Res.string.dash_stat_bookings),
                    (provider?.bookingsCount ?: state.all.size).toString(), Modifier.weight(1f),
                )
            }

            // Weekly chart
            Spacer(Modifier.height(20.dp))
            WeekChart(state.lastSevenDays)

            // Pending
            Spacer(Modifier.height(24.dp))
            SectionHeader(stringResource(Res.string.dash_pending_title), onSeeAll = if (state.pending.isNotEmpty()) openRequests else null)
            Spacer(Modifier.height(12.dp))
            if (state.pending.isEmpty()) {
                Row(
                    Modifier.fillMaxWidth().ykCard(elevation = 2.dp).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(Modifier.size(40.dp).clip(YkRadius.sm).background(Yk.SuccessSoft), contentAlignment = Alignment.Center) {
                        Icon(YkIcons.Check, null, tint = Yk.Success, modifier = Modifier.size(20.dp))
                    }
                    Text(stringResource(Res.string.dash_pending_empty), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.pending.take(3).forEach { req ->
                        ProviderRequestCard(req, onAccept = { model.accept(req.id) }, onReject = { model.reject(req.id) })
                    }
                }
            }

            // Quick actions
            Spacer(Modifier.height(24.dp))
            Text(stringResource(Res.string.dash_quick_title), style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(YkIcons.Plus, stringResource(Res.string.dash_action_add_service), Modifier.weight(1f)) { navigator.push(AddServiceScreen) }
                QuickAction(YkIcons.Ticket, stringResource(Res.string.dash_action_coupons), Modifier.weight(1f)) { navigator.push(ProviderCouponsScreen) }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(YkIcons.Clipboard, stringResource(Res.string.dash_action_requests), Modifier.weight(1f)) { openRequests() }
                QuickAction(YkIcons.Headphones, stringResource(Res.string.dash_action_support), Modifier.weight(1f), badge = stringResource(Res.string.profile_soon)) {}
            }
        }
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Column(
        modifier
            .then(
                if (highlight) Modifier.clip(YkRadius.card).background(Yk.BrandGradient)
                else Modifier.ykCard(elevation = 3.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(38.dp).clip(YkRadius.sm).background(if (highlight) Color.White.copy(alpha = 0.22f) else tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = if (highlight) Color.White else tint, modifier = Modifier.size(19.dp)) }
        Column {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = if (highlight) Color.White else Yk.Ink, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelMedium, color = if (highlight) Color.White.copy(alpha = 0.85f) else Yk.Ink3)
        }
    }
}

@Composable
private fun WeekChart(days: List<ProviderDashboardScreenModel.DayCount>) {
    val language = LocalAppLanguage.current
    val max = (days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val total = days.sumOf { it.count }
    val grow = remember { Animatable(0f) }
    LaunchedEffect(Unit) { grow.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }

    Column(Modifier.fillMaxWidth().ykCard(elevation = 3.dp).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.dash_week_title), style = MaterialTheme.typography.titleMedium, color = Yk.Ink, modifier = Modifier.weight(1f))
            Text(
                stringResource(Res.string.dash_week_total, total),
                style = MaterialTheme.typography.labelMedium,
                color = Yk.OrangeDeep,
                modifier = Modifier.clip(YkRadius.pill).background(Yk.OrangeSoft).padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth().height(120.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            days.forEachIndexed { i, d ->
                val today = i == days.lastIndex
                val fraction = (d.count.toFloat() / max).coerceAtLeast(0.06f)
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                    if (d.count > 0) {
                        Text(d.count.toString(), style = MaterialTheme.typography.labelSmall, color = if (today) Yk.OrangeDeep else Yk.Ink3)
                        Spacer(Modifier.height(4.dp))
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction * 0.82f)
                            .graphicsLayer {
                                scaleY = grow.value
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                            }
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(
                                if (today) Brush.verticalGradient(listOf(Color(0xFFFF8A4C), Yk.OrangeDeep))
                                else Brush.verticalGradient(listOf(Yk.OrangeTint, Yk.OrangeSoft)),
                            ),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            days.forEachIndexed { i, d ->
                Text(
                    dayLabel(d.date.dayOfWeek, language),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == days.lastIndex) Yk.OrangeDeep else Yk.Ink3,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun dayLabel(day: DayOfWeek, language: AppLanguage): String {
    val i = day.ordinal // MONDAY = 0
    return if (language == AppLanguage.Arabic) {
        listOf("إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت", "أحد")[i]
    } else {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[i]
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier.pressable(onClick).ykCard(elevation = 3.dp).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(38.dp).clip(YkRadius.sm).background(Yk.OrangeSoft), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Yk.Orange, modifier = Modifier.size(19.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = Yk.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (badge != null) Text(badge, style = MaterialTheme.typography.labelSmall, color = Yk.Ink3)
        }
    }
}
