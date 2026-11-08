package com.yallakhedma.app.presentation.screens.bookings

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.ErrorBanner
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkFormScreen
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.i18n.asString
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.breq_done
import yallakhidma.shared.generated.resources.breq_listed_price
import yallakhidma.shared.generated.resources.breq_price_ph
import yallakhidma.shared.generated.resources.breq_sent_body
import yallakhidma.shared.generated.resources.breq_sent_title
import yallakhidma.shared.generated.resources.breq_submit
import yallakhidma.shared.generated.resources.breq_subtitle
import yallakhidma.shared.generated.resources.breq_title
import yallakhidma.shared.generated.resources.breq_your_price
import yallakhidma.shared.generated.resources.currency_sar

/** Client proposes a price for a service; the provider then accepts or declines. */
data class BookingRequestScreen(private val service: Service) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<BookingRequestScreenModel>()
        val state by model.state.collectAsState()
        val focus = LocalFocusManager.current
        LaunchedEffect(service.providerId) { model.loadProvider(service.providerId) }

        AnimatedContent(
            targetState = state.submitted,
            transitionSpec = { (fadeIn(tween(260)) + scaleIn(tween(260), initialScale = 0.96f)) togetherWith fadeOut(tween(160)) },
            label = "breq",
        ) { submitted ->
            if (submitted) {
                SentConfirmation(onDone = { navigator.pop() })
            } else {
                YkFormScreen(
                    title = stringResource(Res.string.breq_title),
                    subtitle = stringResource(Res.string.breq_subtitle),
                    onBack = { navigator.pop() },
                    bottomBar = {
                        ErrorBanner(state.error?.asString(), Modifier.padding(bottom = 10.dp))
                        YkButton(
                            text = stringResource(Res.string.breq_submit),
                            onClick = { focus.clearFocus(); model.submit(service) },
                            loading = state.submitting,
                        )
                    },
                ) {
                    // Service summary
                    val provider = state.provider
                    Column(Modifier.fillMaxWidth().ykCard(elevation = 4.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RemoteImage(service.imageUrl, service.title, Modifier.size(72.dp).clip(YkRadius.button)) {
                                Icon(YkIcons.Wrench, null, tint = Yk.Orange.copy(alpha = 0.5f), modifier = Modifier.size(26.dp))
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(service.title, style = MaterialTheme.typography.titleMedium, color = Yk.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                val name = provider?.name?.takeIf { it.isNotBlank() } ?: service.providerName
                                if (name.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Avatar(name, provider?.photoUrl, 22.dp, textSize = MaterialTheme.typography.labelSmall.fontSize)
                                        Text(name, style = MaterialTheme.typography.bodySmall, color = Yk.Ink2, maxLines = 1)
                                    }
                                }
                            }
                        }
                        if (service.priceFrom.isNotBlank()) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Yk.Line))
                            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(Res.string.breq_listed_price), style = MaterialTheme.typography.bodyMedium, color = Yk.Ink2, modifier = Modifier.weight(1f))
                                Text(service.priceFrom, style = MaterialTheme.typography.titleSmall, color = Yk.OrangeDeep)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    YkTextField(
                        value = state.price,
                        onValueChange = model::onPrice,
                        label = stringResource(Res.string.breq_your_price),
                        placeholder = stringResource(Res.string.breq_price_ph),
                        leadingIcon = YkIcons.Wallet,
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                        forceLtr = true,
                        isError = state.error != null,
                        trailing = {
                            Text(
                                stringResource(Res.string.currency_sar),
                                style = MaterialTheme.typography.labelLarge,
                                color = Yk.Ink2,
                                modifier = Modifier.clip(YkRadius.chip).background(Yk.Line).padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SentConfirmation(onDone: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Yk.Canvas)
            .safeTopPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(96.dp).background(Yk.SuccessSoft, YkRadius.pill), contentAlignment = Alignment.Center) {
            Icon(YkIcons.Check, null, tint = Yk.Success, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(Res.string.breq_sent_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(Res.string.breq_sent_body), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        YkButton(text = stringResource(Res.string.breq_done), onClick = onDone)
    }
}
