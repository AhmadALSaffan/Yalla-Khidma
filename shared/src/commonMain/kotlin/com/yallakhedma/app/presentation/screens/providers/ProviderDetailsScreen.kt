package com.yallakhedma.app.presentation.screens.providers

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkButtonStyle
import com.yallakhedma.app.presentation.components.YkIconButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.formatRating
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.screens.common.ServiceListCard
import com.yallakhedma.app.presentation.screens.common.TagChip
import com.yallakhedma.app.presentation.screens.common.VerifiedTick
import com.yallakhedma.app.presentation.screens.services.ServiceDetailsScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.cd_back
import yallakhidma.shared.generated.resources.common_call
import yallakhidma.shared.generated.resources.common_message
import yallakhidma.shared.generated.resources.provider_about
import yallakhidma.shared.generated.resources.provider_about_fallback
import yallakhidma.shared.generated.resources.provider_no_phone
import yallakhidma.shared.generated.resources.provider_no_services
import yallakhidma.shared.generated.resources.provider_services
import yallakhidma.shared.generated.resources.provider_skills
import yallakhidma.shared.generated.resources.provider_stat_bookings
import yallakhidma.shared.generated.resources.provider_stat_jobs
import yallakhidma.shared.generated.resources.provider_stat_rating
import yallakhidma.shared.generated.resources.provider_stat_years
import yallakhidma.shared.generated.resources.provider_verified
import yallakhidma.shared.generated.resources.rating_new

class ProviderDetailsScreen(private val initial: Provider) : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ProviderDetailsScreenModel>()
        val state by model.state.collectAsState()
        val uri = LocalUriHandler.current
        LaunchedEffect(Unit) { model.load(initial.id) }

        val provider = state.provider ?: initial
        val phone = provider.phone.filter { it.isDigit() || it == '+' }

        Box(Modifier.fillMaxSize().background(Yk.Canvas)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 120.dp),
            ) {
                // Cover + overlapping avatar
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .background(Yk.BrandGradient),
                    ) {
                        // Decorative rings
                        Box(
                            Modifier.size(260.dp).align(Alignment.TopEnd).offset(x = 80.dp, y = (-90).dp)
                                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                        )
                        Box(
                            Modifier.size(180.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-50).dp)
                                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                        )
                        YkIconButton(
                            icon = YkIcons.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_back),
                            onClick = { navigator.pop() },
                            background = Color.White.copy(alpha = 0.22f),
                            tint = Color.White,
                            modifier = Modifier.safeTopPadding().padding(start = 16.dp, top = 8.dp),
                        )
                    }
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 56.dp)
                            .size(116.dp)
                            .background(Color.White, CircleShape)
                            .padding(4.dp),
                    ) { Avatar(provider.name, provider.photoUrl, 108.dp, textSize = MaterialTheme.typography.displaySmall.fontSize) }
                }

                Spacer(Modifier.height(68.dp))
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(provider.name, style = MaterialTheme.typography.headlineLarge, color = Yk.Ink, textAlign = TextAlign.Center)
                        if (provider.verified) VerifiedTick(20.dp)
                    }
                    Text(provider.profession, style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (provider.city.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(YkIcons.MapPin, null, tint = Yk.Ink3, modifier = Modifier.size(14.dp))
                                Text(provider.city, style = MaterialTheme.typography.labelLarge, color = Yk.Ink3)
                            }
                        }
                        if (provider.verified) {
                            Row(
                                Modifier.clip(YkRadius.pill).background(Yk.SuccessSoft).padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(YkIcons.ShieldCheck, null, tint = Yk.Success, modifier = Modifier.size(12.dp))
                                Text(stringResource(Res.string.provider_verified), style = MaterialTheme.typography.labelMedium, color = Yk.Success)
                            }
                        }
                    }
                }

                // Stats strip
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .ykCard(elevation = 6.dp)
                        .padding(vertical = 14.dp),
                ) {
                    Stat(
                        value = if (provider.rating > 0) formatRating(provider.rating) else stringResource(Res.string.rating_new),
                        label = stringResource(Res.string.provider_stat_rating),
                        icon = YkIcons.StarFilled,
                        iconTint = Yk.Warning,
                    )
                    StatDivider()
                    Stat(provider.completedJobs.toString(), stringResource(Res.string.provider_stat_jobs))
                    StatDivider()
                    Stat(provider.bookingsCount.toString(), stringResource(Res.string.provider_stat_bookings))
                    if (provider.yearsExperience > 0) {
                        StatDivider()
                        Stat(provider.yearsExperience.toString(), stringResource(Res.string.provider_stat_years))
                    }
                }

                Section(stringResource(Res.string.provider_about)) {
                    Text(
                        provider.bio.ifBlank { stringResource(Res.string.provider_about_fallback, provider.profession) },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Yk.Ink2,
                    )
                }

                if (provider.services.isNotEmpty()) {
                    Section(stringResource(Res.string.provider_skills)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            provider.services.forEach { TagChip(it) }
                        }
                    }
                }

                Section(stringResource(Res.string.provider_services)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        when {
                            state.servicesLoading -> repeat(2) { Box(Modifier.fillMaxWidth().height(84.dp).shimmer(YkRadius.card)) }
                            state.services.isEmpty() -> Text(
                                stringResource(Res.string.provider_no_services),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Yk.Ink3,
                            )
                            else -> state.services.forEach { svc ->
                                ServiceListCard(svc, onClick = { navigator.push(ServiceDetailsScreen(svc)) }, compact = true)
                            }
                        }
                    }
                }
            }

            // Sticky contact bar
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Yk.Canvas)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                if (phone.isBlank()) {
                    Text(
                        stringResource(Res.string.provider_no_phone),
                        style = MaterialTheme.typography.bodySmall,
                        color = Yk.Ink3,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    YkButton(
                        text = stringResource(Res.string.common_message),
                        onClick = { runCatching { uri.openUri("https://wa.me/${phone.removePrefix("+")}") } },
                        style = YkButtonStyle.Outline,
                        enabled = phone.isNotBlank(),
                        leading = { Icon(YkIcons.Message, null, tint = Yk.Ink, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                    )
                    YkButton(
                        text = stringResource(Res.string.common_call),
                        onClick = { runCatching { uri.openUri("tel:$phone") } },
                        enabled = phone.isNotBlank(),
                        leading = { Icon(YkIcons.Phone, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Stat(
    value: String,
    label: String,
    icon: ImageVector? = null,
    iconTint: Color = Yk.Ink,
) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (icon != null) Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = Yk.Ink3)
    }
}

@Composable
private fun StatDivider() {
    Box(Modifier.width(1.dp).fillMaxHeight().padding(vertical = 6.dp).background(Yk.Line))
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Yk.Ink)
        content()
    }
}
