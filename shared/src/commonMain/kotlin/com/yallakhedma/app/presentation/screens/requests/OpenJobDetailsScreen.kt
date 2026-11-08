package com.yallakhedma.app.presentation.screens.requests

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.OpenRequest
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.YkIconButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.screens.chats.ChatScreen
import com.yallakhedma.app.presentation.screens.chats.chatSeed
import com.yallakhedma.app.presentation.screens.common.formatMoney
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.job_eta
import yallakhidma.shared.generated.resources.job_eta_ph
import yallakhidma.shared.generated.resources.job_message
import yallakhidma.shared.generated.resources.job_message_ph
import yallakhidma.shared.generated.resources.job_offer_sent_body
import yallakhidma.shared.generated.resources.job_offer_sent_title
import yallakhidma.shared.generated.resources.job_send_offer
import yallakhidma.shared.generated.resources.job_title
import yallakhidma.shared.generated.resources.job_your_offer
import yallakhidma.shared.generated.resources.job_your_price
import yallakhidma.shared.generated.resources.oreq_eta
import yallakhidma.shared.generated.resources.req_message_client

/** Provider view of an open job: details + send an offer (or see the one already sent). */
data class OpenJobDetailsScreen(private val requestId: String) : Screen {

    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<OpenJobDetailsScreenModel>()
        val state by model.state.collectAsState()
        LaunchedEffect(requestId) { model.load(requestId) }
        val req = state.request
        val canOffer = req != null && req.status == OpenRequest.STATUS_OPEN && state.myOffer == null && !state.sent

        YkFormScreen(
            title = stringResource(Res.string.job_title),
            onBack = { navigator.pop() },
            bottomBar = if (canOffer) {
                {
                    ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 10.dp))
                    YkButton(
                        text = stringResource(Res.string.job_send_offer),
                        onClick = model::send,
                        loading = state.sending,
                        leading = { Icon(YkIcons.Bolt, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
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

            // Client
            Row(
                Modifier.fillMaxWidth().clip(YkRadius.card).background(Yk.Fill).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Avatar(req.clientName, req.clientPhoto.takeIf { it.isNotBlank() }, 44.dp)
                Text(req.clientName, style = MaterialTheme.typography.titleMedium, color = Yk.Ink, modifier = Modifier.weight(1f))
                val me = rememberMe()
                YkIconButton(
                    icon = YkIcons.Message,
                    contentDescription = stringResource(Res.string.req_message_client),
                    onClick = {
                        navigator.push(
                            ChatScreen(
                                chatSeed(
                                    clientId = req.clientId, clientName = req.clientName, clientPhoto = req.clientPhoto,
                                    providerId = me?.id.orEmpty(), providerName = me?.name.orEmpty(),
                                    providerPhoto = me?.photoUrl.orEmpty(), providerProfession = "",
                                ),
                            ),
                        )
                    },
                    tint = Yk.Orange,
                    background = Yk.Canvas,
                    enabled = me != null,
                )
            }
            Spacer(Modifier.height(18.dp))
            RequestSummary(req)

            Spacer(Modifier.height(24.dp))
            val offer = state.myOffer
            AnimatedContent(
                targetState = offer != null || state.sent,
                transitionSpec = { (fadeIn(tween(240)) + scaleIn(initialScale = 0.96f)) togetherWith fadeOut(tween(140)) },
                label = "offer",
            ) { hasOffer ->
                if (hasOffer) {
                    Column(
                        Modifier.fillMaxWidth().clip(YkRadius.card).background(Yk.SuccessSoft).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(36.dp).clip(CircleShape).background(Yk.Success), contentAlignment = Alignment.Center) {
                                Icon(YkIcons.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(if (offer != null) Res.string.job_your_offer else Res.string.job_offer_sent_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Yk.Ink,
                                )
                                if (offer == null) Text(stringResource(Res.string.job_offer_sent_body), style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
                            }
                            if (offer != null) OfferStatusBadge(offer.status)
                        }
                        if (offer != null) {
                            Text("${formatMoney(offer.price)} ${offer.currency}", style = MaterialTheme.typography.headlineSmall, color = Yk.OrangeDeep)
                            if (offer.eta.isNotBlank()) Text(stringResource(Res.string.oreq_eta, offer.eta), style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
                            if (offer.message.isNotBlank()) Text(offer.message, style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2)
                        }
                    }
                } else if (req.status == OpenRequest.STATUS_OPEN) {
                    Column {
                        Text(stringResource(Res.string.job_send_offer), style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
                        Spacer(Modifier.height(12.dp))
                        YkTextField(
                            value = state.price,
                            onValueChange = model::onPrice,
                            label = stringResource(Res.string.job_your_price),
                            placeholder = formatMoney(((req.budgetMin + req.budgetMax) / 2 / 10).toInt() * 10.0),
                            leadingIcon = YkIcons.Wallet,
                            keyboardType = KeyboardType.Decimal,
                            forceLtr = true,
                            trailing = {
                                Text(
                                    req.currency,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Yk.Ink2,
                                    modifier = Modifier.clip(YkRadius.chip).background(Yk.Line).padding(horizontal = 12.dp, vertical = 8.dp),
                                )
                            },
                        )
                        Spacer(Modifier.height(16.dp))
                        YkTextField(
                            value = state.eta,
                            onValueChange = model::onEta,
                            label = stringResource(Res.string.job_eta),
                            placeholder = stringResource(Res.string.job_eta_ph),
                            leadingIcon = YkIcons.Clock,
                        )
                        Spacer(Modifier.height(16.dp))
                        YkTextField(
                            value = state.message,
                            onValueChange = model::onMessage,
                            label = stringResource(Res.string.job_message),
                            placeholder = stringResource(Res.string.job_message_ph),
                            singleLine = false,
                            capitalization = KeyboardCapitalization.Sentences,
                        )
                    }
                }
            }
        }
    }
}
