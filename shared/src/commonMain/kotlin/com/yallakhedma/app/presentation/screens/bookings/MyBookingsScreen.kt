package com.yallakhedma.app.presentation.screens.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val Success = Color(0xFF10B981)
private val Warning = Color(0xFFF59E0B)
private val Danger = Color(0xFFEF4444)

object MyBookingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<MyBookingsScreenModel>()
        val state by screenModel.state.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
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
                Text("حجوزاتي", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(40.dp))
            }

            val kind = when {
                state.loading -> "loading"
                state.bookings.isEmpty() -> "empty"
                else -> "content"
            }
            AnimatedContent(
                targetState = kind,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "my-bookings-state",
                modifier = Modifier.fillMaxSize(),
            ) { state_ ->
                when (state_) {
                    "loading" -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = LogoOrange) }

                    "empty" -> Box(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "ما عندك حجوزات لسا",
                                color = OnSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "اطلب خدمة من قائمة مقدمي الخدمات وستظهر هنا",
                                color = OnSurfaceVariant,
                                fontSize = 13.sp,
                            )
                        }
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.bookings, key = { it.id }) { booking ->
                            BookingRow(
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(300),
                                    fadeOutSpec = tween(200),
                                    placementSpec = tween(350),
                                ),
                                booking = booking,
                                liveImageUrl = state.liveImages[booking.serviceId],
                                onClick = { navigator.push(BookingDetailsScreen(booking)) },
                                onPay = {
                                navigator.push(
                                    PayBookingScreen(
                                        requestId = booking.id,
                                        amount = booking.proposedPrice,
                                        currency = booking.currency,
                                        serviceTitle = booking.serviceTitle.ifBlank { "خدمة" },
                                        providerId = booking.providerId,
                                    )
                                )
                            },
                        )
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun BookingRow(
    booking: ServiceRequest,
    liveImageUrl: String?,
    onClick: () -> Unit,
    onPay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Service thumbnail — prefer the snapshot, fall back to the live image
        // resolved by the screen model (covers bookings made before snapshotting).
        val rawImage = booking.serviceImageUrl.ifBlank { liveImageUrl.orEmpty() }
        val thumbUrl = HomeImageAssets.imageUrl(rawImage)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LogoOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                booking.serviceTitle.firstOrNull()?.toString() ?: "؟",
                color = Primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            if (thumbUrl != null) {
                AsyncImage(
                    model = thumbUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                )
            }
        }

        // Title / provider / date column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = booking.serviceTitle.ifBlank { "خدمة" },
                color = OnSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (booking.providerName.isNotBlank()) {
                Text(booking.providerName, color = OnSurfaceVariant, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(formatDate(booking.createdAt), color = OnSurfaceVariant, fontSize = 11.sp)
                if (booking.servicePrice.isNotBlank()) {
                    Text("•", color = OnSurfaceVariant, fontSize = 11.sp)
                    Text(booking.servicePrice, color = Primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Status chip + pay (when actionable) + chevron
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Crossfade(
                targetState = booking.paid to booking.status,
                animationSpec = tween(280),
                label = "status-chip",
            ) { (paid, status) -> StatusChip(status, paid = paid) }
            val canPay = !booking.paid &&
                booking.status == ServiceRequest.STATUS_ACCEPTED &&
                booking.proposedPrice > 0.0
            if (canPay) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LogoOrange)
                        .clickable(onClick = onPay)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text("ادفع", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Text("‹", color = OnSurfaceVariant, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun StatusChip(status: String, paid: Boolean = false) {
    val (label, bg, fg) = when {
        paid -> Triple("مدفوع", Success.copy(alpha = 0.12f), Success)
        status == ServiceRequest.STATUS_ACCEPTED -> Triple("مقبول", Success.copy(alpha = 0.12f), Success)
        status == ServiceRequest.STATUS_REJECTED -> Triple("مرفوض", Danger.copy(alpha = 0.12f), Danger)
        else -> Triple("قيد الانتظار", Warning.copy(alpha = 0.15f), Warning)
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

private fun formatDate(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val mm = dt.monthNumber.toString().padStart(2, '0')
    val dd = dt.dayOfMonth.toString().padStart(2, '0')
    val hh = dt.hour.toString().padStart(2, '0')
    val mn = dt.minute.toString().padStart(2, '0')
    return "${dt.year}-$mm-$dd  $hh:$mn"
}
