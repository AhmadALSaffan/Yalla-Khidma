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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.foundation.Image
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets
import com.yallakhedma.app.util.CardBrand
import org.jetbrains.compose.resources.painterResource
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

data class BookingDetailsScreen(
    private val booking: ServiceRequest,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<BookingDetailsScreenModel>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(booking.serviceId, booking.providerId) {
            screenModel.load(booking.serviceId, booking.providerId)
        }

        // Prefer the snapshot fields when present (instant), fall back to the
        // live Service/Provider docs (so old bookings that pre-date snapshotting
        // still show the picture the provider added).
        val liveService = state.service
        val liveProvider = state.provider
        val serviceImage = booking.serviceImageUrl.ifBlank { liveService?.imageUrl.orEmpty() }
        val serviceDescription = booking.serviceDescription.ifBlank { liveService?.description.orEmpty() }
        val servicePrice = booking.servicePrice.ifBlank { liveService?.priceFrom.orEmpty() }
        val serviceTitle = booking.serviceTitle.ifBlank { liveService?.title.orEmpty().ifBlank { "خدمة" } }
        val categoryTag = booking.serviceCategoryTag.ifBlank { liveService?.categoryTag.orEmpty() }
        val providerName = booking.providerName.ifBlank { liveProvider?.name.orEmpty() }
        val providerPhoto = booking.providerPhotoUrl.ifBlank { liveProvider?.photoUrl.orEmpty() }
        val providerProfession = booking.providerProfession.ifBlank { liveProvider?.profession.orEmpty() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState()),
        ) {
            HeroHeader(
                imageUrl = HomeImageAssets.imageUrl(serviceImage),
                onBack = { navigator.pop() },
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = serviceTitle,
                            color = OnSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        StatusChipLarge(status = booking.status, paid = booking.paid)
                    }
                    if (categoryTag.isNotBlank()) {
                        Text(categoryTag, color = LogoOrange, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (servicePrice.isNotBlank()) {
                        Text(
                            "السعر المقترح من مقدم الخدمة: $servicePrice",
                            color = OnSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }
                    // Headline price: if paid, show what was actually charged
                    // (with the original struck-through alongside if a coupon
                    // dropped it). Otherwise show the user's offer.
                    if (booking.paid) {
                        val recordedPaid = booking.paidAmount.takeIf { it > 0.0 } ?: booking.proposedPrice
                        val discounted = recordedPaid < booking.proposedPrice
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                "المبلغ المدفوع: ${formatMoneyDetail(recordedPaid)} ${booking.currency}",
                                color = Primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            if (discounted) {
                                Text(
                                    "${formatMoneyDetail(booking.proposedPrice)} ${booking.currency}",
                                    color = OnSurfaceVariant,
                                    fontSize = 13.sp,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                                )
                            }
                        }
                    } else if (booking.proposedPrice > 0.0) {
                        Text(
                            "عرضك: ${formatMoneyDetail(booking.proposedPrice)} ${booking.currency}",
                            color = Primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                // Pay action / paid badge
                when {
                    booking.paid -> SectionCard(title = "حالة الدفع") {
                        // Header row — "تم الدفع ✓" + brand logo + last 4
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "تم الدفع ✓",
                                color = Success,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (booking.paymentMethodBrand.isNotBlank()) {
                                    val brand = CardBrand.fromKey(booking.paymentMethodBrand)
                                    Image(
                                        painter = painterResource(brand.drawable),
                                        contentDescription = brand.displayName,
                                        modifier = Modifier.size(width = 40.dp, height = 26.dp),
                                    )
                                }
                                if (booking.paymentMethodLast4.isNotBlank()) {
                                    Text(
                                        "•••• ${booking.paymentMethodLast4}",
                                        color = OnSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }

                        // Amount breakdown — only meaningful if we have either
                        // a coupon snapshot OR a recorded paid amount.
                        val hasCoupon = booking.couponCode.isNotBlank() || booking.couponLabel.isNotBlank()
                        val recordedPaid = booking.paidAmount.takeIf { it > 0.0 } ?: booking.proposedPrice
                        if (hasCoupon || booking.proposedPrice > 0.0) {
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Background)
                                    .padding(12.dp),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    BreakdownRow(
                                        label = "السعر الأصلي",
                                        value = "${formatMoneyDetail(booking.proposedPrice)} ${booking.currency}",
                                    )
                                    if (hasCoupon) {
                                        BreakdownRow(
                                            label = if (booking.couponCode.isNotBlank())
                                                "كوبون (${booking.couponCode})" else "كوبون خصم",
                                            value = "- ${booking.couponLabel}",
                                            valueColor = Success,
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(OutlineVariant.copy(alpha = 0.4f)),
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            "المبلغ المدفوع",
                                            color = OnSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            "${formatMoneyDetail(recordedPaid)} ${booking.currency}",
                                            color = Primary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    booking.status == ServiceRequest.STATUS_ACCEPTED && booking.proposedPrice > 0.0 -> {
                        AppPrimaryButton(
                            text = "ادفع الآن (${formatMoneyDetail(booking.proposedPrice)} ${booking.currency})",
                            onClick = {
                                navigator.push(
                                    PayBookingScreen(
                                        requestId = booking.id,
                                        amount = booking.proposedPrice,
                                        currency = booking.currency,
                                        serviceTitle = serviceTitle,
                                        providerId = booking.providerId,
                                    )
                                )
                            },
                        )
                    }
                }

                if (serviceDescription.isNotBlank()) {
                    SectionCard(title = "وصف الخدمة") {
                        Text(serviceDescription, color = OnSurface, fontSize = 14.sp)
                    }
                }

                if (providerName.isNotBlank() || providerPhoto.isNotBlank()) {
                    SectionCard(title = "مقدم الخدمة") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ProviderAvatar(
                                photoUrl = HomeImageAssets.imageUrl(providerPhoto),
                                fallbackInitial = providerName.firstOrNull()?.toString() ?: "؟",
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    providerName.ifBlank { "—" },
                                    color = OnSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (providerProfession.isNotBlank()) {
                                    Text(providerProfession, color = OnSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                SectionCard(title = "تفاصيل الحجز") {
                    MetaRow("تاريخ الحجز", formatDateTime(booking.createdAt))
                    if (booking.id.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        MetaRow("رقم الحجز", booking.id.take(8).uppercase())
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(imageUrl: String?, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(LogoOrange),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // Subtle dark overlay for back-button contrast
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)))
        }
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = 46.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) { Text("→", color = Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = OnSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun BreakdownRow(label: String, value: String, valueColor: Color = OnSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 12.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 13.sp)
        Text(value, color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProviderAvatar(photoUrl: String?, fallbackInitial: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(LogoOrange.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(fallbackInitial, color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
        }
    }
}

@Composable
private fun StatusChipLarge(status: String, paid: Boolean = false) {
    val (label, bg, fg) = when {
        paid -> Triple("مدفوع", Success.copy(alpha = 0.12f), Success)
        status == ServiceRequest.STATUS_ACCEPTED -> Triple("مقبول", Success.copy(alpha = 0.12f), Success)
        status == ServiceRequest.STATUS_REJECTED -> Triple("مرفوض", Danger.copy(alpha = 0.12f), Danger)
        else -> Triple("قيد الانتظار", Warning.copy(alpha = 0.15f), Warning)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatMoneyDetail(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    if (rounded % 1.0 == 0.0) return rounded.toLong().toString()
    val whole = rounded.toLong()
    val fracTimes100 = ((rounded - whole) * 100).let { if (it < 0) -it else it }
    val frac = fracTimes100.toLong().toString().padStart(2, '0')
    return "$whole.$frac"
}

private fun formatDateTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return "—"
    val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val mm = dt.monthNumber.toString().padStart(2, '0')
    val dd = dt.dayOfMonth.toString().padStart(2, '0')
    val hh = dt.hour.toString().padStart(2, '0')
    val mn = dt.minute.toString().padStart(2, '0')
    return "${dt.year}-$mm-$dd  $hh:$mn"
}
