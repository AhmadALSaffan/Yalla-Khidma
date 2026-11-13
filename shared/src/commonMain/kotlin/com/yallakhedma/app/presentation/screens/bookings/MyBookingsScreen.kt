package com.yallakhedma.app.presentation.screens.bookings

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.yallakhedma.app.presentation.screens.requests.SegmentedTabs
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.LocalBottomBarInset
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkChip
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.pressable
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.screens.explore.ExploreScreen
import com.yallakhedma.app.presentation.screens.main.ClientMainScreen
import com.yallakhedma.app.presentation.screens.main.LocalInTabs
import com.yallakhedma.app.presentation.screens.main.LocalTabController
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.seg_bookings
import yallakhidma.shared.generated.resources.seg_my_requests
import yallakhidma.shared.generated.resources.bookings_empty_body
import yallakhidma.shared.generated.resources.bookings_empty_cta
import yallakhidma.shared.generated.resources.bookings_empty_title
import yallakhidma.shared.generated.resources.bookings_filter_accepted
import yallakhidma.shared.generated.resources.bookings_filter_all
import yallakhidma.shared.generated.resources.bookings_filter_paid
import yallakhidma.shared.generated.resources.bookings_filter_pending
import yallakhidma.shared.generated.resources.bookings_pay
import yallakhidma.shared.generated.resources.bookings_service_fallback
import yallakhidma.shared.generated.resources.bookings_status_accepted
import yallakhidma.shared.generated.resources.bookings_status_paid
import yallakhidma.shared.generated.resources.bookings_status_pending
import yallakhidma.shared.generated.resources.bookings_status_rejected
import yallakhidma.shared.generated.resources.bookings_subtitle
import yallakhidma.shared.generated.resources.bookings_title

private enum class BookingFilter(val label: StringResource) {
    All(Res.string.bookings_filter_all),
    Pending(Res.string.bookings_filter_pending),
    Accepted(Res.string.bookings_filter_accepted),
    Paid(Res.string.bookings_filter_paid);

    fun matches(b: ServiceRequest): Boolean = when (this) {
        All -> true
        Pending -> b.status == ServiceRequest.STATUS_PENDING
        Accepted -> b.status == ServiceRequest.STATUS_ACCEPTED && !b.paid
        Paid -> b.paid
    }
}

/** Client bookings (tab content; also works pushed standalone). */
object MyBookingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<MyBookingsScreenModel>()
        val state by model.state.collectAsState()
        val reqModel = koinScreenModel<com.yallakhedma.app.presentation.screens.requests.MyRequestsScreenModel>()
        val reqState by reqModel.state.collectAsState()
        var segment by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }
        val inTabs = LocalInTabs.current
        val tabs = LocalTabController.current
        val bottomInset = LocalBottomBarInset.current
        var filter by rememberSaveable { mutableStateOf(BookingFilter.All) }
        val fallbackTitle = stringResource(Res.string.bookings_service_fallback)

        Column(Modifier.fillMaxSize().background(Yk.Canvas).safeTopPadding()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                if (!inTabs) YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.bookings_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(Res.string.bookings_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
            }
            SegmentedTabs(
                labels = listOf(stringResource(Res.string.seg_bookings), stringResource(Res.string.seg_my_requests)),
                selected = segment,
                onSelect = { segment = it },
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
            )
            if (segment == 0) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BookingFilter.entries.forEach { f ->
                    YkChip(stringResource(f.label), filter == f, { filter = f })
                }
            }

            val visible = state.bookings.filter(filter::matches)
            AnimatedContent(
                targetState = filter,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                modifier = Modifier.weight(1f),
                label = "bookings-filter",
            ) { _ ->
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = bottomInset + 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when {
                        state.loading -> items(4) { Box(Modifier.fillMaxWidth().height(112.dp).shimmer(YkRadius.card)) }
                        visible.isEmpty() -> item {
                            EmptyState(
                                icon = YkIcons.CalendarCheck,
                                title = stringResource(Res.string.bookings_empty_title),
                                body = stringResource(Res.string.bookings_empty_body),
                                actionLabel = stringResource(Res.string.bookings_empty_cta),
                                onAction = { tabs?.select(ClientMainScreen.TAB_EXPLORE) ?: navigator.push(ExploreScreen()) },
                            )
                        }
                        else -> items(visible, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                liveImage = state.liveImages[booking.serviceId],
                                modifier = Modifier.animateItem(fadeInSpec = tween(260), fadeOutSpec = tween(180), placementSpec = tween(320)),
                                onClick = { navigator.push(BookingDetailsScreen(booking)) },
                                onPay = {
                                    navigator.push(
                                        PayBookingScreen(
                                            requestId = booking.id,
                                            amount = booking.proposedPrice,
                                            currency = booking.currency,
                                            serviceTitle = booking.serviceTitle.ifBlank { fallbackTitle },
                                            providerId = booking.providerId,
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }
            }
            } else {
                com.yallakhedma.app.presentation.screens.requests.MyRequestsList(
                    state = reqState,
                    onOpen = { navigator.push(com.yallakhedma.app.presentation.screens.requests.OpenRequestDetailsScreen(it.id)) },
                    onNew = { navigator.push(com.yallakhedma.app.presentation.screens.requests.NewRequestScreen()) },
                    bottomPadding = bottomInset + 24.dp,
                    modifier = Modifier.weight(1f).padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: ServiceRequest,
    liveImage: String?,
    onClick: () -> Unit,
    onPay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canPay = !booking.paid && booking.status == ServiceRequest.STATUS_ACCEPTED && booking.proposedPrice > 0.0
    Column(
        modifier
            .fillMaxWidth()
            .pressable(onClick)
            .ykCard()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RemoteImage(
                booking.serviceImageUrl.ifBlank { liveImage.orEmpty() },
                booking.serviceTitle,
                Modifier.size(64.dp).clip(YkRadius.button),
            ) { Icon(YkIcons.Wrench, null, tint = Yk.Orange.copy(alpha = 0.5f), modifier = Modifier.size(24.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    booking.serviceTitle.ifBlank { stringResource(Res.string.bookings_service_fallback) },
                    style = MaterialTheme.typography.titleMedium,
                    color = Yk.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (booking.providerName.isNotBlank()) {
                    Text(booking.providerName, style = MaterialTheme.typography.bodySmall, color = Yk.Ink2, maxLines = 1)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(YkIcons.Clock, null, tint = Yk.Ink3, modifier = Modifier.size(12.dp))
                    Text(formatDate(booking.createdAt), style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
                }
            }
            StatusBadge(booking)
        }
        if (canPay) {
            Row(
                Modifier.fillMaxWidth().clip(YkRadius.sm).background(Yk.OrangeSoft).padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${formatAmount(booking.proposedPrice)} ${booking.currency}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Yk.OrangeDeep,
                    modifier = Modifier.weight(1f),
                )
                YkButton(
                    text = stringResource(Res.string.bookings_pay),
                    onClick = onPay,
                    height = 40.dp,
                    modifier = Modifier.fillMaxWidth(0.45f),
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(booking: ServiceRequest) {
    val (label, bg, fg) = when {
        booking.paid -> Triple(Res.string.bookings_status_paid, Yk.SuccessSoft, Yk.Success)
        booking.status == ServiceRequest.STATUS_ACCEPTED -> Triple(Res.string.bookings_status_accepted, Color(0xFFE8F1FE), Yk.Info)
        booking.status == ServiceRequest.STATUS_REJECTED -> Triple(Res.string.bookings_status_rejected, Yk.DangerSoft, Yk.Danger)
        else -> Triple(Res.string.bookings_status_pending, Yk.WarningSoft, Color(0xFFB45309))
    }
    Text(
        stringResource(label),
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        modifier = Modifier.clip(YkRadius.pill).background(bg).padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

private fun formatAmount(v: Double): String =
    if (v % 1.0 == 0.0) v.toLong().toString() else ((v * 100).toLong() / 100.0).toString()

private fun formatDate(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val mm = dt.monthNumber.toString().padStart(2, '0')
    val dd = dt.dayOfMonth.toString().padStart(2, '0')
    val hh = dt.hour.toString().padStart(2, '0')
    val mn = dt.minute.toString().padStart(2, '0')
    return "${dt.year}-$mm-$dd · $hh:$mn"
}
