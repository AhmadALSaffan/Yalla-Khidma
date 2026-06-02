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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
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
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.screens.bookings.BookingRequestScreen
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets
import com.yallakhedma.app.presentation.screens.providers.ProviderDetailsScreen

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val Primary = Color(0xFFA73A00)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val Secondary = Color(0xFF5D3FD3)
private val Success = Color(0xFF10B981)
private val Danger = Color(0xFFEF4444)
private val StarYellow = Color(0xFFF59E0B)

@Composable
private fun MetaChip(text: String, accent: Color = OnSurfaceVariant) {
    Text(
        text = text,
        color = accent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

private fun formatRating(rating: Double): String {
    val rounded = (rating * 10).toInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

class ServiceDetailsScreen(private val service: Service) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ServiceDetailsScreenModel>()
        val state by screenModel.state.collectAsState()
        LaunchedEffect(Unit) { screenModel.loadProvider(service.providerId) }

        Box(modifier = Modifier.fillMaxSize().background(Background)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 104.dp),
            ) {
                // Image header with a floating back button.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(PrimaryFixed),
                ) {
                    val url = HomeImageAssets.imageUrl(service.imageUrl)
                    if (url != null) {
                        AsyncImage(
                            model = url,
                            contentDescription = service.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 46.dp, start = 20.dp)
                            .align(Alignment.TopStart)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { navigator.pop() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("→", color = Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (service.categoryTag.isNotBlank()) {
                        Text(
                            text = service.categoryTag,
                            color = Secondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Secondary.copy(alpha = 0.10f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                    Text(
                        text = service.title,
                        color = OnSurface,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 30.sp,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = service.priceFrom,
                            color = Primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        if (service.distance.isNotBlank()) {
                            Text(
                                text = service.distance,
                                color = OnSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(SurfaceContainerLow)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }

                    // Meta chips: duration + service rating (shown when present).
                    if (service.durationText.isNotBlank() || service.rating > 0.0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (service.durationText.isNotBlank()) {
                                MetaChip("⏱ ${service.durationText}")
                            }
                            if (service.rating > 0.0) {
                                MetaChip("★ ${formatRating(service.rating)}", accent = StarYellow)
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(OutlineVariant.copy(alpha = 0.4f)),
                    )
                    Spacer(Modifier.height(4.dp))

                    if (service.description.isNotBlank()) {
                        Text(
                            text = "تفاصيل الخدمة",
                            color = OnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = service.description,
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                        )
                    }

                    // Provider card — uses the live provider (photo + rating)
                    // when loaded, falling back to the denormalized name.
                    val provider = state.provider
                    val displayName = provider?.name?.takeIf { it.isNotBlank() }
                        ?: service.providerName
                    if (displayName.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "مقدم الخدمة",
                            color = OnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceContainerLow)
                                .then(
                                    if (provider != null) {
                                        Modifier.clickable {
                                            navigator.push(ProviderDetailsScreen(provider))
                                        }
                                    } else Modifier,
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryFixed),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = displayName.firstOrNull()?.toString() ?: "؟",
                                    color = Primary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                val pUrl = provider?.let { HomeImageAssets.imageUrl(it.photoUrl) }
                                if (pUrl != null) {
                                    AsyncImage(
                                        model = pUrl,
                                        contentDescription = displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = displayName,
                                    color = OnSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (provider != null && provider.profession.isNotBlank()) {
                                    Text(
                                        text = provider.profession,
                                        color = OnSurfaceVariant,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                            if (provider != null && provider.rating > 0.0) {
                                Text(
                                    text = "★ ${formatRating(provider.rating)}",
                                    color = StarYellow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            if (provider != null) {
                                Text("‹", color = OnSurfaceVariant, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            // Sticky bottom booking bar.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Background)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                AppPrimaryButton(
                    text = "احجز الخدمة",
                    onClick = { navigator.push(BookingRequestScreen(service)) },
                )
            }
        }
    }
}
