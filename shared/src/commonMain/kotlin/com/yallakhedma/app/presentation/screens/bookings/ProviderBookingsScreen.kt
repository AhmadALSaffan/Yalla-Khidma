package com.yallakhedma.app.presentation.screens.bookings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.presentation.screens.bookings.ProviderBookingsScreenModel.Filter
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets
import kotlinx.datetime.toLocalDateTime

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val Success = Color(0xFF10B981)
private val Warning = Color(0xFFF59E0B)
private val Danger = Color(0xFFEF4444)

object ProviderBookingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ProviderBookingsScreenModel>()
        val state by screenModel.state.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize().background(Background),
        ) {
            // Orange header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(LogoOrange)
                    .padding(start = 20.dp, end = 20.dp, top = 46.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable { navigator.pop() },
                    contentAlignment = Alignment.Center,
                ) { Text("→", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                Text("إدارة الحجوزات", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(40.dp))
            }

            // Filter chips
            FilterRow(
                current = state.filter,
                counts = state.counts,
                onPick = screenModel::setFilter,
            )

            AnimatedContent(
                targetState = when {
                    state.loading -> StateKind.Loading
                    state.filtered.isEmpty() -> StateKind.Empty
                    else -> StateKind.Content
                },
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "bookings-state",
                modifier = Modifier.fillMaxSize(),
            ) { kind ->
                when (kind) {
                    StateKind.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = LogoOrange) }

                    StateKind.Empty -> Box(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "ما في حجوزات بهذا الفلتر",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                        )
                    }

                    StateKind.Content -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp, end = 20.dp, top = 14.dp, bottom = 32.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.filtered, key = { it.id }) { booking ->
                            ProviderBookingCard(
                                booking = booking,
                                onAccept = { screenModel.accept(booking.id) },
                                onReject = { screenModel.reject(booking.id) },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(300),
                                    fadeOutSpec = tween(200),
                                    placementSpec = tween(350),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class StateKind { Loading, Empty, Content }

@Composable
private fun FilterRow(
    current: Filter,
    counts: Map<Filter, Int>,
    onPick: (Filter) -> Unit,
) {
    val items = listOf(
        Filter.All to "الكل",
        Filter.Pending to "قيد الانتظار",
        Filter.Accepted to "مقبول",
        Filter.Paid to "مدفوع",
        Filter.Rejected to "مرفوض",
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.first }) { (filter, label) ->
            val selected = filter == current
            val count = counts[filter] ?: 0
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) LogoOrange else SurfaceContainerLow)
                    .clickable { onPick(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        label,
                        color = if (selected) Color.White else OnSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selected) Color.White.copy(alpha = 0.25f) else Primary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        ) {
                            Text(
                                count.toString(),
                                color = if (selected) Color.White else Primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderBookingCard(
    booking: ServiceRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top row — client + service + status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Avatar (client photo or initial)
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(PrimaryFixed),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    booking.clientName.firstOrNull()?.toString() ?: "؟",
                    color = Primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                val url = HomeImageAssets.imageUrl(booking.clientPhotoUrl)
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = booking.clientName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    booking.serviceTitle.ifBlank { "خدمة" },
                    color = OnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    booking.clientName.ifBlank { "عميل" },
                    color = OnSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
            Crossfade(
                targetState = statusKey(booking),
                animationSpec = tween(280),
                label = "status-chip",
            ) { key ->
                StatusChip(key)
            }
        }

        // Proposed price + date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (booking.proposedPrice > 0.0) {
                Text(
                    "عرض العميل: ${formatMoney(booking.proposedPrice)} ${booking.currency}",
                    color = Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Spacer(Modifier.size(1.dp))
            }
            Text(formatDate(booking.createdAt), color = OnSurfaceVariant, fontSize = 11.sp)
        }

        // Actions — only when pending
        AnimatedVisibility(
            visible = booking.status == ServiceRequest.STATUS_PENDING,
            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 },
            exit = fadeOut(tween(160)),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LogoOrange)
                        .clickable(onClick = onAccept),
                    contentAlignment = Alignment.Center,
                ) { Text("قبول", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, OutlineVariant), RoundedCornerShape(10.dp))
                        .clickable(onClick = onReject),
                    contentAlignment = Alignment.Center,
                ) { Text("رفض", color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            }
        }

        // Paid badge (when paid)
        AnimatedVisibility(
            visible = booking.paid,
            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Success.copy(alpha = 0.10f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "تم الدفع ✓",
                    color = Success,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (booking.paymentMethodLast4.isNotBlank()) {
                    Text(
                        "•••• ${booking.paymentMethodLast4}",
                        color = OnSurface,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

private enum class StatusKey { Pending, Accepted, Rejected, Paid }

private fun statusKey(b: ServiceRequest): StatusKey = when {
    b.paid -> StatusKey.Paid
    b.status == ServiceRequest.STATUS_ACCEPTED -> StatusKey.Accepted
    b.status == ServiceRequest.STATUS_REJECTED -> StatusKey.Rejected
    else -> StatusKey.Pending
}

@Composable
private fun StatusChip(key: StatusKey) {
    val (label, bg, fg) = when (key) {
        StatusKey.Paid -> Triple("مدفوع", Success.copy(alpha = 0.12f), Success)
        StatusKey.Accepted -> Triple("مقبول", Success.copy(alpha = 0.12f), Success)
        StatusKey.Rejected -> Triple("مرفوض", Danger.copy(alpha = 0.12f), Danger)
        StatusKey.Pending -> Triple("قيد الانتظار", Warning.copy(alpha = 0.15f), Warning)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatMoney(amount: Double): String {
    val r = (amount * 100).toLong() / 100.0
    return if (r % 1.0 == 0.0) r.toLong().toString() else r.toString()
}

private fun formatDate(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val dt = kotlinx.datetime.Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val mm = dt.monthNumber.toString().padStart(2, '0')
    val dd = dt.dayOfMonth.toString().padStart(2, '0')
    val hh = dt.hour.toString().padStart(2, '0')
    val mn = dt.minute.toString().padStart(2, '0')
    return "${dt.year}-$mm-$dd  $hh:$mn"
}
