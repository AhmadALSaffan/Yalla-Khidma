package com.yallakhedma.app.presentation.screens.services

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets
import com.yallakhedma.app.util.ClientLocationHolder
import com.yallakhedma.app.util.PickedLocation
import com.yallakhedma.app.util.formatDistanceKm
import com.yallakhedma.app.util.haversineKm
import com.yallakhedma.app.util.rememberLocationPicker
import org.koin.compose.koinInject

// Stitch palette (consistent with home / categories / providers).
private val Background = Color(0xFFFFFFFF)
private val Surface = Color(0xFFFFFFFF)
private val SurfaceContainerHigh = Color(0xFFE2E7FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val Secondary = Color(0xFF5D3FD3)

object AllServicesScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AllServicesScreenModel>()
        val state by screenModel.state.collectAsState()
        val locationHolder = koinInject<ClientLocationHolder>()
        val clientLocation by locationHolder.location.collectAsState()
        val locationPicker = rememberLocationPicker { picked ->
            if (picked != null) locationHolder.set(picked)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            Header(onBack = { navigator.pop() })

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LocationChip(label = clientLocation?.label, onClick = { locationPicker() })
                when {
                    state.loading && state.services.isEmpty() ->
                        ServiceGrid(count = 6) { index -> ServiceGridCardSkeleton(index) }
                    state.services.isEmpty() -> EmptyState()
                    else -> {
                        // Sort services by distance (when location is set), then
                        // group by category so each section keeps that order.
                        val sorted = sortByDistance(state.services, clientLocation)
                        val groups = sorted.groupBy { it.categoryTag.ifBlank { "أخرى" } }
                        Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                            groups.forEach { (category, services) ->
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    CategoryLabel(category, services.size)
                                    ServiceGrid(count = services.size) { index ->
                                        val svc = services[index]
                                        ServiceGridCard(
                                            service = svc,
                                            index = index,
                                            onClick = { navigator.push(ServiceDetailsScreen(svc)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(LogoOrange)
            .padding(start = 20.dp, end = 20.dp, top = 46.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Text("→", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "كل الخدمات القريبة",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.size(40.dp))
        }
        Text(
            text = "تصفح كل الخدمات المتوفرة بالقرب منك",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun CategoryLabel(category: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(50))
                .background(LogoOrange),
        )
        Text(
            text = category,
            color = OnSurface,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "($count)",
            color = OnSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun ServiceGrid(count: Int, cell: @Composable (index: Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        (0 until count).chunked(2).forEach { rowIndices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                rowIndices.forEach { index ->
                    Box(modifier = Modifier.weight(1f)) { cell(index) }
                }
                repeat(2 - rowIndices.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ServiceGridCard(service: Service, index: Int, onClick: () -> Unit) {
    StaggeredEntrance(index) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Surface)
                .clickable(onClick = onClick),
        ) {
            // Landscape photo — shorter than a square so the card stays compact.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
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
            }
            // Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = service.title,
                    color = OnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = service.priceFrom,
                        color = Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (service.distance.isNotBlank()) {
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = service.distance,
                            color = OnSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceGridCardSkeleton(index: Int) {
    StaggeredEntrance(index) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Surface)
                .border(BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.25f)), RoundedCornerShape(22.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .background(SurfaceContainerHigh),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh),
                )
            }
        }
    }
}

/** Fades + lifts each card into place with a per-index delay for a staggered feel. */
@Composable
private fun StaggeredEntrance(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val delay = (index * 55).coerceAtMost(550)
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 450, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "alpha",
    )
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else 48f,
        animationSpec = tween(durationMillis = 450, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "translateY",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translateY
            },
    ) {
        content()
    }
}

/** Sort services by Haversine distance from [origin]; unknown coords (0,0) go last. */
private fun sortByDistance(services: List<Service>, origin: PickedLocation?): List<Service> {
    if (origin == null) return services
    return services
        .map { svc ->
            val d = if (svc.latitude != 0.0 || svc.longitude != 0.0) {
                haversineKm(origin.latitude, origin.longitude, svc.latitude, svc.longitude)
            } else Double.MAX_VALUE
            val labeled = if (d != Double.MAX_VALUE) svc.copy(distance = "📍 ${formatDistanceKm(d)}") else svc
            labeled to d
        }
        .sortedBy { it.second }
        .map { it.first }
}

@Composable
private fun LocationChip(label: String?, onClick: () -> Unit) {
    val text = if (label.isNullOrBlank()) "📍 الأقرب لي" else "📍 $label • تغيير"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PrimaryFixed.copy(alpha = 0.4f))
            .border(BorderStroke(1.dp, Primary.copy(alpha = 0.4f)), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, color = Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "ما في خدمات متاحة حالياً",
            color = OnSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}
