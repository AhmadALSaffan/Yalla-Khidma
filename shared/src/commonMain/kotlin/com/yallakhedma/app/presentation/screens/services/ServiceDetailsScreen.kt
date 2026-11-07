package com.yallakhedma.app.presentation.screens.services

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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.RatingPill
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIconButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.pressable
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.screens.bookings.BookingRequestScreen
import com.yallakhedma.app.presentation.screens.common.TagChip
import com.yallakhedma.app.presentation.screens.common.VerifiedTick
import com.yallakhedma.app.presentation.screens.providers.ProviderDetailsScreen
import com.yallakhedma.app.presentation.theme.Yk
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.cd_back
import yallakhidma.shared.generated.resources.service_book
import yallakhidma.shared.generated.resources.service_details
import yallakhidma.shared.generated.resources.service_duration
import yallakhidma.shared.generated.resources.service_price_from
import yallakhidma.shared.generated.resources.service_provider

class ServiceDetailsScreen(private val service: Service) : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ServiceDetailsScreenModel>()
        val state by model.state.collectAsState()
        LaunchedEffect(Unit) { model.loadProvider(service.providerId) }

        Box(Modifier.fillMaxSize().background(Yk.Canvas)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 120.dp),
            ) {
                // Image hero
                Box(Modifier.fillMaxWidth().height(300.dp)) {
                    RemoteImage(service.imageUrl, service.title, Modifier.fillMaxSize()) {
                        Icon(YkIcons.Wrench, null, tint = Yk.Orange.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                    }
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Yk.Ink.copy(alpha = 0.35f), Color.Transparent, Color.Transparent)),
                        ),
                    )
                    YkIconButton(
                        icon = YkIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.cd_back),
                        onClick = { navigator.pop() },
                        background = Color.White,
                        modifier = Modifier.statusBarsPadding().padding(start = 16.dp, top = 8.dp),
                    )
                }

                // Content sheet overlapping the hero
                Column(
                    Modifier
                        .offset(y = (-28).dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Yk.Canvas)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (service.categoryTag.isNotBlank()) TagChip(service.categoryTag)
                        if (service.rating > 0.0) RatingPill(service.rating)
                    }
                    Text(service.title, style = MaterialTheme.typography.headlineLarge, color = Yk.Ink)

                    // Quick facts
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Fact(YkIcons.CreditCard, stringResource(Res.string.service_price_from), service.priceFrom, Modifier.weight(1f))
                        if (service.durationText.isNotBlank()) {
                            Fact(YkIcons.Clock, stringResource(Res.string.service_duration), service.durationText, Modifier.weight(1f))
                        } else if (service.distance.isNotBlank()) {
                            Fact(YkIcons.MapPin, "", service.distance, Modifier.weight(1f))
                        }
                    }

                    if (service.description.isNotBlank()) {
                        Text(stringResource(Res.string.service_details), style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
                        Text(service.description, style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
                    }

                    val provider = state.provider
                    val displayName = provider?.name?.takeIf { it.isNotBlank() } ?: service.providerName
                    if (displayName.isNotBlank()) {
                        Text(stringResource(Res.string.service_provider), style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .then(if (provider != null) Modifier.pressable({ navigator.push(ProviderDetailsScreen(provider)) }) else Modifier)
                                .ykCard(elevation = 6.dp)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Avatar(displayName, provider?.photoUrl, 52.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(displayName, style = MaterialTheme.typography.titleMedium, color = Yk.Ink)
                                    if (provider?.verified == true) VerifiedTick()
                                }
                                if (provider != null && provider.profession.isNotBlank()) {
                                    Text(provider.profession, style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
                                }
                            }
                            if (provider != null) {
                                RatingPill(provider.rating)
                                Icon(YkIcons.ChevronForward, null, tint = Yk.Ink3, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Sticky booking bar
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .shadow(16.dp, ambientColor = Yk.Ink.copy(alpha = 0.1f), spotColor = Yk.Ink.copy(alpha = 0.1f))
                    .background(Yk.Canvas)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column {
                    Text(stringResource(Res.string.service_price_from), style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
                    Text(service.priceFrom, style = MaterialTheme.typography.titleLarge, color = Yk.OrangeDeep)
                }
                YkButton(
                    text = stringResource(Res.string.service_book),
                    onClick = { navigator.push(BookingRequestScreen(service)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun Fact(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(16.dp)).background(Yk.Fill).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Yk.Canvas), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Yk.Orange, modifier = Modifier.size(18.dp))
        }
        Column {
            if (label.isNotBlank()) Text(label, style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
            Text(value, style = MaterialTheme.typography.titleSmall, color = Yk.Ink, maxLines = 1)
        }
    }
}
