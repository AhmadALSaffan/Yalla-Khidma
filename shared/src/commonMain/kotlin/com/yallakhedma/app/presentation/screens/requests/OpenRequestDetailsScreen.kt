package com.yallakhedma.app.presentation.screens.requests

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.OpenRequest
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.StaggeredItem
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkButtonStyle
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.screens.chats.ChatScreen
import com.yallakhedma.app.presentation.screens.chats.chatListTime
import com.yallakhedma.app.presentation.screens.chats.chatSeed
import com.yallakhedma.app.presentation.screens.providers.ProviderDetailsScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.job_details
import yallakhidma.shared.generated.resources.nreq_budget
import yallakhidma.shared.generated.resources.nreq_location
import yallakhidma.shared.generated.resources.nreq_time
import yallakhidma.shared.generated.resources.oreq_accepted_note
import yallakhidma.shared.generated.resources.oreq_close
import yallakhidma.shared.generated.resources.oreq_no_offers
import yallakhidma.shared.generated.resources.oreq_offers_title
import yallakhidma.shared.generated.resources.oreq_title

/** Client view of one of their open requests and the offers it received. */
data class OpenRequestDetailsScreen(private val requestId: String) : Screen {

    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<OpenRequestDetailsScreenModel>()
        val state by model.state.collectAsState()
        LaunchedEffect(requestId) { model.load(requestId) }
        val req = state.request

        YkFormScreen(
            title = stringResource(Res.string.oreq_title),
            onBack = { navigator.pop() },
            bottomBar = if (req?.status == OpenRequest.STATUS_OPEN) {
                {
                    ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 10.dp))
                    YkButton(
                        text = stringResource(Res.string.oreq_close),
                        onClick = model::close,
                        style = YkButtonStyle.Outline,
                        height = 50.dp,
                    )
                }
            } else null,
        ) {
            if (req == null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.fillMaxWidth().height(160.dp).shimmer(YkRadius.card))
                    Box(Modifier.fillMaxWidth().height(120.dp).shimmer(YkRadius.card))
                }
                return@YkFormScreen
            }
            RequestSummary(req)

            AnimatedVisibility(state.accepted || req.status == OpenRequest.STATUS_ASSIGNED, enter = fadeIn() + expandVertically()) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp).clip(YkRadius.sm).background(Yk.SuccessSoft).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(YkIcons.CalendarCheck, null, tint = Yk.Success, modifier = Modifier.size(18.dp))
                    Text(stringResource(Res.string.oreq_accepted_note), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink)
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.oreq_offers_title), style = MaterialTheme.typography.headlineSmall, color = Yk.Ink, modifier = Modifier.weight(1f))
                if (state.offers.isNotEmpty()) {
                    Text(
                        state.offers.size.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Yk.OrangeDeep,
                        modifier = Modifier.clip(YkRadius.pill).background(Yk.OrangeSoft).padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (state.offers.isEmpty()) {
                EmptyState(YkIcons.Hourglass, stringResource(Res.string.oreq_no_offers))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.offers.forEachIndexed { i, offer ->
                        StaggeredItem(i) {
                            OfferCard(
                                offer = offer,
                                canAccept = req.status == OpenRequest.STATUS_OPEN && !state.accepted,
                                accepting = state.acceptingId == offer.id,
                                onAccept = { model.accept(offer) },
                                onMessage = {
                                    navigator.push(
                                        ChatScreen(
                                            chatSeed(
                                                clientId = req.clientId, clientName = req.clientName, clientPhoto = req.clientPhoto,
                                                providerId = offer.providerId, providerName = offer.providerName,
                                                providerPhoto = offer.providerPhoto, providerProfession = offer.providerProfession,
                                            ),
                                        ),
                                    )
                                },
                                onOpenProvider = {
                                    navigator.push(
                                        ProviderDetailsScreen(
                                            Provider(
                                                id = offer.providerId, name = offer.providerName, photoUrl = offer.providerPhoto,
                                                profession = offer.providerProfession, rating = offer.providerRating, verified = offer.providerVerified,
                                            ),
                                        ),
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

/** Shared summary block (client + provider views). */
@Composable
fun ColumnScope.RequestSummary(req: OpenRequest) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            req.localizedCategory(),
            style = MaterialTheme.typography.labelLarge,
            color = Yk.OrangeDeep,
            modifier = Modifier.clip(YkRadius.pill).background(Yk.OrangeSoft).padding(horizontal = 12.dp, vertical = 5.dp),
        )
        OpenRequestStatusBadge(req.status)
        Spacer(Modifier.weight(1f))
        Text(chatListTime(req.createdAt), style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
    }
    Spacer(Modifier.height(14.dp))
    Text(stringResource(Res.string.job_details), style = MaterialTheme.typography.titleSmall, color = Yk.Ink3)
    Spacer(Modifier.height(6.dp))
    Text(req.description, style = MaterialTheme.typography.bodyLarge, color = Yk.Ink)
    if (req.images.isNotEmpty()) {
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            req.images.forEach { RemoteImage(it, null, Modifier.size(110.dp).clip(YkRadius.sm)) }
        }
    }
    Spacer(Modifier.height(16.dp))
    Column(
        Modifier.fillMaxWidth().clip(YkRadius.card).background(Yk.Fill).padding(horizontal = 14.dp, vertical = 4.dp),
    ) {
        InfoLine(YkIcons.Wallet, stringResource(Res.string.nreq_budget), budgetText(req.budgetMin, req.budgetMax, req.currency))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Yk.Line))
        InfoLine(YkIcons.Clock, stringResource(Res.string.nreq_time), timingLabel(req))
        if (req.locationLabel.isNotBlank()) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(Yk.Line))
            InfoLine(YkIcons.MapPin, stringResource(Res.string.nreq_location), req.locationLabel)
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = Yk.Ink3, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = Yk.Ink, maxLines = 1)
    }
}
