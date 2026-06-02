package com.yallakhedma.app.presentation.screens.providers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets
import com.yallakhedma.app.presentation.screens.services.ServiceDetailsScreen

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val PrimaryContainer = Color(0xFFFF5C00)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val Secondary = Color(0xFF5D3FD3)
private val Tertiary = Color(0xFF00696E)
private val StarYellow = Color(0xFFF59E0B)

class ProviderDetailsScreen(private val initial: Provider) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ProviderDetailsScreenModel>()
        val state by screenModel.state.collectAsState()
        LaunchedEffect(Unit) { screenModel.load(initial.id) }

        val provider = state.provider ?: initial

        Box(modifier = Modifier.fillMaxSize().background(Background)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp),
            ) {
                // Cover (back button on the RIGHT) + overlapping avatar
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Brush.linearGradient(listOf(Primary, PrimaryContainer))),
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 46.dp, start = 20.dp)
                                .align(Alignment.TopStart)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { navigator.pop() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("→", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 50.dp)
                            .size(108.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLow)
                            .border(BorderStroke(4.dp, Color.White), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = provider.name.firstOrNull()?.toString() ?: "؟",
                            color = Primary,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        val url = HomeImageAssets.imageUrl(provider.photoUrl)
                        if (url != null) {
                            AsyncImage(
                                model = url,
                                contentDescription = provider.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(62.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(provider.name, color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        if (provider.verified) {
                            Box(
                                modifier = Modifier.size(20.dp).clip(CircleShape).background(Secondary),
                                contentAlignment = Alignment.Center,
                            ) { Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                    Text(provider.profession, color = OnSurfaceVariant, fontSize = 14.sp, textAlign = TextAlign.Center)
                    if (provider.city.isNotBlank()) {
                        Text("📍 ${provider.city}", color = OnSurfaceVariant, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(Modifier.weight(1f), formatRating(provider.rating), "التقييم", StarYellow, "★")
                    StatCard(Modifier.weight(1f), provider.completedJobs.toString(), "مكتمل", Tertiary)
                    StatCard(Modifier.weight(1f), provider.bookingsCount.toString(), "حجز", Primary)
                    if (provider.yearsExperience > 0) {
                        StatCard(Modifier.weight(1f), provider.yearsExperience.toString(), "سنوات", Secondary)
                    }
                }

                Spacer(Modifier.height(24.dp))

                SectionBlock("نبذة") {
                    Text(
                        text = provider.bio.ifBlank { "مزود خدمة موثوق في مجال ${provider.profession}." },
                        color = OnSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                    )
                }

                if (provider.services.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    SectionBlock("تخصصاته") {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            provider.services.forEach { s ->
                                Text(
                                    text = s,
                                    color = Primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(PrimaryContainer.copy(alpha = 0.12f))
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }

                // Actual service offerings (from the services collection).
                if (state.services.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    SectionBlock("خدماته") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.services.forEach { svc ->
                                ServiceRow(svc, onClick = { navigator.push(ServiceDetailsScreen(svc)) })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Background)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ContactButton(Modifier.weight(1f), "رسالة", filled = false)
                ContactButton(Modifier.weight(1f), "اتصال", filled = true)
            }
        }
    }
}

@Composable
private fun ServiceRow(service: Service, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(PrimaryFixed),
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
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(service.title, color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(service.priceFrom, color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text("‹", color = OnSurfaceVariant, fontSize = 20.sp)
    }
}

@Composable
private fun SectionBlock(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    value: String,
    label: String,
    valueColor: Color = OnSurface,
    leading: String? = null,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (leading != null) Text(leading, color = valueColor, fontSize = 14.sp)
            Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, color = OnSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun ContactButton(modifier: Modifier, label: String, filled: Boolean) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (filled) LogoOrange else SurfaceContainerLow)
            .border(
                if (filled) BorderStroke(0.dp, Color.Transparent)
                else BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                RoundedCornerShape(12.dp),
            )
            .clickable { /* TODO: wire phone / chat */ },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (filled) Color.White else Primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatRating(rating: Double): String {
    if (rating == 0.0) return "—"
    val rounded = (rating * 10).toInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}
