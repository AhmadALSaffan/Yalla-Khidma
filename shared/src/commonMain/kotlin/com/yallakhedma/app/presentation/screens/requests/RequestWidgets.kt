package com.yallakhedma.app.presentation.screens.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.domain.model.Offer
import com.yallakhedma.app.domain.model.OpenRequest
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.pressable
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.i18n.AppLanguage
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.screens.chats.chatListTime
import com.yallakhedma.app.presentation.screens.chats.shortDate
import com.yallakhedma.app.presentation.screens.common.VerifiedTick
import com.yallakhedma.app.presentation.screens.common.formatMoney
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.home_post_body
import yallakhidma.shared.generated.resources.home_post_cta
import yallakhidma.shared.generated.resources.home_post_title
import yallakhidma.shared.generated.resources.jobs_offered
import yallakhidma.shared.generated.resources.oreq_accept
import yallakhidma.shared.generated.resources.oreq_eta
import yallakhidma.shared.generated.resources.oreq_offers_n
import yallakhidma.shared.generated.resources.oreq_status_assigned
import yallakhidma.shared.generated.resources.oreq_status_closed
import yallakhidma.shared.generated.resources.oreq_status_open
import yallakhidma.shared.generated.resources.offer_accepted
import yallakhidma.shared.generated.resources.offer_declined
import yallakhidma.shared.generated.resources.offer_pending
import yallakhidma.shared.generated.resources.time_date
import yallakhidma.shared.generated.resources.time_today
import yallakhidma.shared.generated.resources.time_tomorrow
import yallakhidma.shared.generated.resources.time_week

fun budgetText(min: Double, max: Double, currency: String): String =
    "${formatMoney(min)} – ${formatMoney(max)} $currency"

@Composable
fun timingLabel(request: OpenRequest): String = when (request.timing) {
    OpenRequest.TIMING_TODAY -> stringResource(Res.string.time_today)
    OpenRequest.TIMING_TOMORROW -> stringResource(Res.string.time_tomorrow)
    OpenRequest.TIMING_DATE -> if (request.preferredDate > 0) shortDate(request.preferredDate) else stringResource(Res.string.time_date)
    else -> stringResource(Res.string.time_week)
}

@Composable
fun OpenRequest.localizedCategory(): String = categoryName(LocalAppLanguage.current == AppLanguage.Arabic)

@Composable
fun OpenRequestStatusBadge(status: String) {
    val (label, fg, bg) = when (status) {
        OpenRequest.STATUS_ASSIGNED -> Triple(stringResource(Res.string.oreq_status_assigned), Yk.Success, Yk.SuccessSoft)
        OpenRequest.STATUS_CLOSED -> Triple(stringResource(Res.string.oreq_status_closed), Yk.Ink3, Yk.Fill)
        else -> Triple(stringResource(Res.string.oreq_status_open), Yk.OrangeDeep, Yk.OrangeSoft)
    }
    Text(label, style = MaterialTheme.typography.labelMedium, color = fg, modifier = Modifier.clip(YkRadius.pill).background(bg).padding(horizontal = 10.dp, vertical = 4.dp))
}

@Composable
fun OfferStatusBadge(status: String) {
    val (label, fg, bg) = when (status) {
        Offer.STATUS_ACCEPTED -> Triple(stringResource(Res.string.offer_accepted), Yk.Success, Yk.SuccessSoft)
        Offer.STATUS_DECLINED -> Triple(stringResource(Res.string.offer_declined), Yk.Ink3, Yk.Fill)
        else -> Triple(stringResource(Res.string.offer_pending), Yk.Warning, Yk.WarningSoft)
    }
    Text(label, style = MaterialTheme.typography.labelMedium, color = fg, modifier = Modifier.clip(YkRadius.pill).background(bg).padding(horizontal = 10.dp, vertical = 4.dp))
}

@Composable
private fun MetaChip(icon: ImageVector, text: String) {
    Row(
        Modifier.clip(YkRadius.pill).background(Yk.Fill).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, null, tint = Yk.Ink3, modifier = Modifier.size(13.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = Yk.Ink2, maxLines = 1)
    }
}

/**
 * An open request as a card. Clients see the offer count; providers see the
 * client and whether they already sent an offer ([myOffer]).
 */
@Composable
fun OpenRequestCard(
    request: OpenRequest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    providerView: Boolean = false,
    myOffer: Offer? = null,
) {
    Column(
        modifier.fillMaxWidth().pressable(onClick).ykCard().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (request.images.isNotEmpty()) {
                RemoteImage(request.images.first(), null, Modifier.size(52.dp).clip(YkRadius.sm))
            } else {
                Box(Modifier.size(52.dp).clip(YkRadius.sm).background(Yk.OrangeSoft), contentAlignment = Alignment.Center) {
                    Icon(YkIcons.Clipboard, null, tint = Yk.Orange, modifier = Modifier.size(22.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(request.localizedCategory(), style = MaterialTheme.typography.labelLarge, color = Yk.OrangeDeep, maxLines = 1)
                Text(
                    request.description,
                    style = MaterialTheme.typography.titleSmall,
                    color = Yk.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!providerView) OpenRequestStatusBadge(request.status)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MetaChip(YkIcons.Wallet, budgetText(request.budgetMin, request.budgetMax, request.currency))
            MetaChip(YkIcons.Clock, timingLabel(request))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (providerView) {
                Avatar(request.clientName, request.clientPhoto.takeIf { it.isNotBlank() }, 24.dp)
                Spacer(Modifier.width(8.dp))
                Text(request.clientName, style = MaterialTheme.typography.bodySmall, color = Yk.Ink2, maxLines = 1, modifier = Modifier.weight(1f))
                if (myOffer != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(YkIcons.Check, null, tint = Yk.Success, modifier = Modifier.size(14.dp))
                        Text(stringResource(Res.string.jobs_offered), style = MaterialTheme.typography.labelMedium, color = Yk.Success)
                    }
                } else {
                    Text(chatListTime(request.createdAt), style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
                }
            } else {
                if (request.locationLabel.isNotBlank()) {
                    Icon(YkIcons.MapPin, null, tint = Yk.Ink3, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(request.locationLabel, style = MaterialTheme.typography.bodySmall, color = Yk.Ink3, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                } else {
                    Spacer(Modifier.weight(1f))
                }
                val count = request.offersCount
                Text(
                    stringResource(Res.string.oreq_offers_n, count),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (count > 0) Color.White else Yk.Ink3,
                    modifier = Modifier.clip(YkRadius.pill).background(if (count > 0) Yk.Orange else Yk.Fill).padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

/** A provider's offer as the client sees it. */
@Composable
fun OfferCard(
    offer: Offer,
    canAccept: Boolean,
    accepting: Boolean,
    onAccept: () -> Unit,
    onMessage: () -> Unit,
    onOpenProvider: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accepted = offer.status == Offer.STATUS_ACCEPTED
    Column(
        modifier
            .fillMaxWidth()
            .ykCard(elevation = if (accepted) 12.dp else 6.dp)
            .then(if (accepted) Modifier.border(2.dp, Yk.Success, YkRadius.card) else Modifier)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(offer.providerName, offer.providerPhoto.takeIf { it.isNotBlank() }, 46.dp, modifier = Modifier.pressable(onOpenProvider))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(offer.providerName, style = MaterialTheme.typography.titleMedium, color = Yk.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (offer.providerVerified) VerifiedTick(14.dp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (offer.providerRating > 0) {
                        Icon(YkIcons.StarFilled, null, tint = Yk.Warning, modifier = Modifier.size(12.dp))
                        Text(formatMoney(offer.providerRating), style = MaterialTheme.typography.labelMedium, color = Yk.Ink2)
                        Text("·", color = Yk.Ink3)
                    }
                    Text(offer.providerProfession, style = MaterialTheme.typography.bodySmall, color = Yk.Ink3, maxLines = 1)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${formatMoney(offer.price)} ${offer.currency}", style = MaterialTheme.typography.headlineSmall, color = Yk.OrangeDeep)
                if (offer.eta.isNotBlank()) Text(stringResource(Res.string.oreq_eta, offer.eta), style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
            }
        }
        if (offer.message.isNotBlank()) {
            Text(
                offer.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Yk.Ink2,
                modifier = Modifier.fillMaxWidth().clip(YkRadius.sm).background(Yk.Fill).padding(12.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(CircleShape).border(1.dp, Yk.Line, CircleShape).pressable(onMessage),
                contentAlignment = Alignment.Center,
            ) { Icon(YkIcons.Message, null, tint = Yk.Ink, modifier = Modifier.size(20.dp)) }
            when {
                canAccept && offer.status == Offer.STATUS_PENDING -> YkButton(
                    text = stringResource(Res.string.oreq_accept),
                    onClick = onAccept,
                    loading = accepting,
                    height = 46.dp,
                    leading = { Icon(YkIcons.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f),
                )
                else -> {
                    Spacer(Modifier.weight(1f))
                    OfferStatusBadge(offer.status)
                }
            }
        }
    }
}

/** Home call-to-action: post an open request. */
@Composable
fun PostRequestCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .shadow(10.dp, YkRadius.card, ambientColor = Yk.Ink.copy(alpha = 0.25f), spotColor = Yk.Ink.copy(alpha = 0.25f))
            .clip(YkRadius.card)
            .background(Yk.Night)
            .pressable(onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(48.dp).clip(YkRadius.sm).background(Yk.Orange), contentAlignment = Alignment.Center) {
            Icon(YkIcons.Edit, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(stringResource(Res.string.home_post_title), style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(stringResource(Res.string.home_post_body), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(Res.string.home_post_cta),
                style = MaterialTheme.typography.labelLarge,
                color = Yk.Night,
                modifier = Modifier.clip(YkRadius.pill).background(Color.White).padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}

/** The signed-in user (null while loading / signed out). */
@Composable
fun rememberMe(): com.yallakhedma.app.domain.model.User? =
    org.koin.compose.koinInject<com.yallakhedma.app.domain.repository.AuthRepository>()
        .currentUser.collectAsState(initial = null).value
