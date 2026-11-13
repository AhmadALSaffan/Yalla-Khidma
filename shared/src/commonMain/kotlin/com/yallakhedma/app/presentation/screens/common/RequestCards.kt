package com.yallakhedma.app.presentation.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkButtonStyle
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.pressable
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.req_message_client
import yallakhidma.shared.generated.resources.action_accept
import yallakhidma.shared.generated.resources.action_reject
import yallakhidma.shared.generated.resources.bookings_service_fallback
import yallakhidma.shared.generated.resources.bookings_status_accepted
import yallakhidma.shared.generated.resources.bookings_status_paid
import yallakhidma.shared.generated.resources.bookings_status_pending
import yallakhidma.shared.generated.resources.bookings_status_rejected
import yallakhidma.shared.generated.resources.dash_client_fallback
import yallakhidma.shared.generated.resources.dash_client_offer
import yallakhidma.shared.generated.resources.requests_paid_via

/** Colored status pill for a booking request (paid wins over status). */
@Composable
fun RequestStatusBadge(request: ServiceRequest) {
    val (label, bg, fg) = when {
        request.paid -> Triple(Res.string.bookings_status_paid, Yk.SuccessSoft, Yk.Success)
        request.status == ServiceRequest.STATUS_ACCEPTED -> Triple(Res.string.bookings_status_accepted, Color(0xFFE8F1FE), Yk.Info)
        request.status == ServiceRequest.STATUS_REJECTED -> Triple(Res.string.bookings_status_rejected, Yk.DangerSoft, Yk.Danger)
        else -> Triple(Res.string.bookings_status_pending, Yk.WarningSoft, Color(0xFFB45309))
    }
    Text(
        stringResource(label),
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        modifier = Modifier.clip(YkRadius.pill).background(bg).padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

fun formatMoney(v: Double): String =
    if (v % 1.0 == 0.0) v.toLong().toString() else ((v * 100).toLong() / 100.0).toString()

fun formatDateTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val mm = dt.monthNumber.toString().padStart(2, '0')
    val dd = dt.dayOfMonth.toString().padStart(2, '0')
    val hh = dt.hour.toString().padStart(2, '0')
    val mn = dt.minute.toString().padStart(2, '0')
    return "${dt.year}-$mm-$dd · $hh:$mn"
}

/**
 * A booking request as the provider sees it: client, service, offer, status.
 * Pending requests show Accept / Decline.
 */
@Composable
fun ProviderRequestCard(
    request: ServiceRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onMessage: (() -> Unit)? = null,
) {
    val pending = request.status == ServiceRequest.STATUS_PENDING
    Column(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressable(onClick) else Modifier)
            .ykCard()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(request.clientName, request.clientPhotoUrl, 46.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    request.serviceTitle.ifBlank { stringResource(Res.string.bookings_service_fallback) },
                    style = MaterialTheme.typography.titleMedium,
                    color = Yk.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    request.clientName.ifBlank { stringResource(Res.string.dash_client_fallback) },
                    style = MaterialTheme.typography.bodySmall,
                    color = Yk.Ink2,
                    maxLines = 1,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(YkIcons.Clock, null, tint = Yk.Ink3, modifier = Modifier.size(12.dp))
                    Text(formatDateTime(request.createdAt), style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
                }
            }
            RequestStatusBadge(request)
        }

        if (request.proposedPrice > 0.0) {
            Row(
                Modifier.fillMaxWidth().clip(YkRadius.sm).background(Yk.Fill).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(YkIcons.Wallet, null, tint = Yk.Ink3, modifier = Modifier.size(16.dp))
                Text(
                    stringResource(Res.string.dash_client_offer),
                    style = MaterialTheme.typography.bodySmall,
                    color = Yk.Ink2,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                )
                Text(
                    "${formatMoney(request.proposedPrice)} ${request.currency}",
                    style = MaterialTheme.typography.titleSmall,
                    color = Yk.OrangeDeep,
                )
            }
        }

        if (request.paid && request.paymentMethodLast4.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(YkIcons.CreditCard, null, tint = Yk.Success, modifier = Modifier.size(14.dp))
                Text(
                    stringResource(Res.string.requests_paid_via, request.paymentMethodBrand, request.paymentMethodLast4),
                    style = MaterialTheme.typography.labelMedium,
                    color = Yk.Success,
                )
            }
        }

        if (pending) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (onMessage != null) {
                    com.yallakhedma.app.presentation.components.YkIconButton(
                        icon = YkIcons.Message,
                        contentDescription = stringResource(Res.string.req_message_client),
                        onClick = onMessage,
                        bordered = true,
                        size = 44.dp,
                        iconSize = 18.dp,
                    )
                }
                YkButton(
                    text = stringResource(Res.string.action_reject),
                    onClick = onReject,
                    style = YkButtonStyle.Outline,
                    height = 44.dp,
                    modifier = Modifier.weight(1f),
                )
                YkButton(
                    text = stringResource(Res.string.action_accept),
                    onClick = onAccept,
                    height = 44.dp,
                    leading = { Icon(YkIcons.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f),
                )
            }
        } else if (onMessage != null) {
            YkButton(
                text = stringResource(Res.string.req_message_client),
                onClick = onMessage,
                style = YkButtonStyle.Outline,
                height = 44.dp,
                leading = { Icon(YkIcons.Message, null, tint = Yk.Ink, modifier = Modifier.size(16.dp)) },
            )
        }
    }
}
