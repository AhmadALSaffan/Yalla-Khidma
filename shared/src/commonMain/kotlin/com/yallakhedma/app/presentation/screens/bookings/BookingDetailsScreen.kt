package com.yallakhedma.app.presentation.screens.bookings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIconButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.screens.common.RequestStatusBadge
import com.yallakhedma.app.presentation.screens.common.TagChip
import com.yallakhedma.app.presentation.screens.common.formatDateTime
import com.yallakhedma.app.presentation.screens.common.formatMoney
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.CardBrand
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.bdet_coupon
import yallakhidma.shared.generated.resources.bdet_coupon_generic
import yallakhidma.shared.generated.resources.bdet_date
import yallakhidma.shared.generated.resources.bdet_info
import yallakhidma.shared.generated.resources.bdet_listed
import yallakhidma.shared.generated.resources.bdet_original
import yallakhidma.shared.generated.resources.bdet_paid
import yallakhidma.shared.generated.resources.bdet_paid_amount
import yallakhidma.shared.generated.resources.bdet_pay_now
import yallakhidma.shared.generated.resources.bdet_payment
import yallakhidma.shared.generated.resources.bdet_ref
import yallakhidma.shared.generated.resources.bdet_rejected
import yallakhidma.shared.generated.resources.bdet_waiting
import yallakhidma.shared.generated.resources.bdet_your_offer
import yallakhidma.shared.generated.resources.bookings_service_fallback
import yallakhidma.shared.generated.resources.cd_back
import yallakhidma.shared.generated.resources.service_details
import yallakhidma.shared.generated.resources.service_provider
import yallakhidma.shared.generated.resources.bdet_message_pro

data class BookingDetailsScreen(
    private val booking: ServiceRequest,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<BookingDetailsScreenModel>()
        val state by model.state.collectAsState()
        LaunchedEffect(booking.serviceId, booking.providerId) { model.load(booking.serviceId, booking.providerId) }

        // Snapshot fields first (instant), live docs as fallback for old bookings.
        val liveService = state.service
        val liveProvider = state.provider
        val serviceImage = booking.serviceImageUrl.ifBlank { liveService?.imageUrl.orEmpty() }
        val description = booking.serviceDescription.ifBlank { liveService?.description.orEmpty() }
        val listedPrice = booking.servicePrice.ifBlank { liveService?.priceFrom.orEmpty() }
        val fallbackTitle = stringResource(Res.string.bookings_service_fallback)
        val title = booking.serviceTitle.ifBlank { liveService?.title.orEmpty().ifBlank { fallbackTitle } }
        val category = booking.serviceCategoryTag.ifBlank { liveService?.categoryTag.orEmpty() }
        val providerName = booking.providerName.ifBlank { liveProvider?.name.orEmpty() }
        val providerPhoto = booking.providerPhotoUrl.ifBlank { liveProvider?.photoUrl.orEmpty() }
        val providerProfession = booking.providerProfession.ifBlank { liveProvider?.profession.orEmpty() }
        val money = { v: Double -> "${formatMoney(v)} ${booking.currency}" }
        val paidAmount = booking.paidAmount.takeIf { it > 0.0 } ?: booking.proposedPrice
        val canPay = !booking.paid && booking.status == ServiceRequest.STATUS_ACCEPTED && booking.proposedPrice > 0.0

        Box(Modifier.fillMaxSize().background(Yk.Fill)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = if (canPay) 110.dp else 32.dp)) {
                // Hero
                Box(Modifier.fillMaxWidth().height(240.dp)) {
                    RemoteImage(serviceImage, title, Modifier.fillMaxSize()) {
                        Icon(YkIcons.Wrench, null, tint = Yk.Orange.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
                    }
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Yk.Ink.copy(alpha = 0.35f), Color.Transparent))))
                    YkIconButton(
                        icon = YkIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.cd_back),
                        onClick = { navigator.pop() },
                        background = Color.White,
                        modifier = Modifier.safeTopPadding().padding(start = 16.dp, top = 8.dp),
                    )
                }

                Column(
                    Modifier
                        .offset(y = (-24).dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Yk.Fill)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (category.isNotBlank()) TagChip(category)
                        RequestStatusBadge(booking)
                    }
                    Text(title, style = MaterialTheme.typography.headlineLarge, color = Yk.Ink)

                    // Amount card
                    Column(Modifier.fillMaxWidth().ykCard(elevation = 4.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(if (booking.paid) Res.string.bdet_paid_amount else Res.string.bdet_your_offer),
                            style = MaterialTheme.typography.labelLarge,
                            color = Yk.Ink3,
                        )
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                money(if (booking.paid) paidAmount else booking.proposedPrice),
                                style = MaterialTheme.typography.displaySmall,
                                color = Yk.OrangeDeep,
                            )
                            if (booking.paid && paidAmount < booking.proposedPrice) {
                                Text(
                                    money(booking.proposedPrice),
                                    style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough),
                                    color = Yk.Ink3,
                                    modifier = Modifier.padding(bottom = 6.dp),
                                )
                            }
                        }
                        if (listedPrice.isNotBlank()) {
                            Text(stringResource(Res.string.bdet_listed, listedPrice), style = MaterialTheme.typography.bodySmall, color = Yk.Ink3)
                        }
                    }

                    // Status-specific block
                    when {
                        booking.paid -> PaymentCard(booking, money, paidAmount)
                        booking.status == ServiceRequest.STATUS_REJECTED ->
                            Notice(YkIcons.Close, stringResource(Res.string.bdet_rejected), Yk.DangerSoft, Yk.Danger)
                        booking.status == ServiceRequest.STATUS_PENDING ->
                            Notice(YkIcons.Hourglass, stringResource(Res.string.bdet_waiting), Yk.WarningSoft, Color(0xFFB45309))
                    }

                    if (description.isNotBlank()) {
                        Section(stringResource(Res.string.service_details)) {
                            Text(description, style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
                        }
                    }

                    if (providerName.isNotBlank()) {
                        Section(stringResource(Res.string.service_provider)) {
                            Row(
                                Modifier.fillMaxWidth().ykCard(elevation = 2.dp).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Avatar(providerName, providerPhoto, 48.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(providerName, style = MaterialTheme.typography.titleMedium, color = Yk.Ink)
                                    if (providerProfession.isNotBlank()) {
                                        Text(providerProfession, style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
                                    }
                                }
                                if (booking.providerId.isNotBlank()) {
                                    com.yallakhedma.app.presentation.components.YkIconButton(
                                        icon = YkIcons.Message,
                                        contentDescription = stringResource(Res.string.bdet_message_pro),
                                        onClick = {
                                            navigator.push(
                                                com.yallakhedma.app.presentation.screens.chats.ChatScreen(
                                                    com.yallakhedma.app.presentation.screens.chats.chatSeed(
                                                        clientId = booking.clientId, clientName = booking.clientName, clientPhoto = booking.clientPhotoUrl,
                                                        providerId = booking.providerId, providerName = providerName,
                                                        providerPhoto = providerPhoto, providerProfession = providerProfession,
                                                    ),
                                                ),
                                            )
                                        },
                                        tint = Yk.Orange,
                                        background = Yk.OrangeSoft,
                                    )
                                }
                            }
                        }
                    }

                    Section(stringResource(Res.string.bdet_info)) {
                        Column(Modifier.fillMaxWidth().ykCard(elevation = 2.dp).padding(horizontal = 16.dp, vertical = 6.dp)) {
                            InfoRow(YkIcons.Clock, stringResource(Res.string.bdet_date), formatDateTime(booking.createdAt))
                            if (booking.id.isNotBlank()) {
                                Box(Modifier.fillMaxWidth().height(1.dp).background(Yk.Line))
                                InfoRow(YkIcons.Clipboard, stringResource(Res.string.bdet_ref), booking.id.take(8).uppercase())
                            }
                        }
                    }
                }
            }

            if (canPay) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Yk.Canvas)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    YkButton(
                        text = stringResource(Res.string.bdet_pay_now, money(booking.proposedPrice)),
                        onClick = {
                            navigator.push(
                                PayBookingScreen(
                                    requestId = booking.id,
                                    amount = booking.proposedPrice,
                                    currency = booking.currency,
                                    serviceTitle = title,
                                    providerId = booking.providerId,
                                ),
                            )
                        },
                        leading = { Icon(YkIcons.CreditCard, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentCard(booking: ServiceRequest, money: (Double) -> String, paidAmount: Double) {
    Section(stringResource(Res.string.bdet_payment)) {
        Column(Modifier.fillMaxWidth().ykCard(elevation = 2.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(YkIcons.ShieldCheck, null, tint = Yk.Success, modifier = Modifier.size(18.dp))
                    Text(stringResource(Res.string.bdet_paid), style = MaterialTheme.typography.titleSmall, color = Yk.Success)
                }
                if (booking.paymentMethodBrand.isNotBlank()) {
                    val brand = CardBrand.fromKey(booking.paymentMethodBrand)
                    Image(painterResource(brand.drawable), brand.displayName, Modifier.size(width = 36.dp, height = 24.dp))
                    Spacer(Modifier.size(6.dp))
                }
                if (booking.paymentMethodLast4.isNotBlank()) {
                    Text("•••• ${booking.paymentMethodLast4}", style = MaterialTheme.typography.labelLarge, color = Yk.Ink)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Yk.Line))
            Breakdown(stringResource(Res.string.bdet_original), money(booking.proposedPrice))
            if (booking.couponCode.isNotBlank() || booking.couponLabel.isNotBlank()) {
                Breakdown(
                    if (booking.couponCode.isNotBlank()) stringResource(Res.string.bdet_coupon, booking.couponCode)
                    else stringResource(Res.string.bdet_coupon_generic),
                    "- ${booking.couponLabel}",
                    Yk.Success,
                )
            }
            Breakdown(stringResource(Res.string.bdet_paid_amount), money(paidAmount), Yk.OrangeDeep, bold = true)
        }
    }
}

@Composable
private fun Breakdown(label: String, value: String, color: Color = Yk.Ink, bold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2, modifier = Modifier.weight(1f))
        Text(value, style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun Notice(icon: ImageVector, text: String, bg: Color, fg: Color) {
    Row(
        Modifier.fillMaxWidth().clip(YkRadius.card).background(bg).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = fg)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = Yk.Ink3, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = Yk.Ink)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
        content()
    }
}
