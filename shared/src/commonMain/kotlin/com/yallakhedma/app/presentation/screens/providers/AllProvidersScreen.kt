package com.yallakhedma.app.presentation.screens.providers

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
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets

// Stitch palette (consistent with the home / categories screens).
private val Background = Color(0xFFFFFFFF)
private val Surface = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val SurfaceContainerHigh = Color(0xFFE2E7FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val PrimaryFixed = Color(0xFFFFDBCE)
private val StarYellow = Color(0xFFF59E0B)

object AllProvidersScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AllProvidersScreenModel>()
        val state by screenModel.state.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            Header(onBack = { navigator.pop() })

            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                when {
                    state.loading && state.providers.isEmpty() ->
                        ProviderGrid(count = 6) { index -> ProviderGridCardSkeleton(index) }
                    state.providers.isEmpty() -> EmptyState()
                    else -> ProviderGrid(count = state.providers.size) { index ->
                        val p = state.providers[index]
                        ProviderGridCard(
                            provider = p,
                            index = index,
                            onClick = { navigator.push(ProviderDetailsScreen(p)) },
                        )
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
                text = "كل المزودين",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.size(40.dp))
        }
        Text(
            text = "تصفح كل مزودي الخدمات الموثوقين",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun ProviderGrid(count: Int, cell: @Composable (index: Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        (0 until count).chunked(2).forEach { rowIndices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                rowIndices.forEach { index ->
                    Box(modifier = Modifier.weight(1f)) { cell(index) }
                }
                // Pad a trailing odd item so the lone card keeps its column width.
                repeat(2 - rowIndices.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ProviderGridCard(provider: Provider, index: Int, onClick: () -> Unit) {
    StaggeredEntrance(index) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Surface)
                .clickable(onClick = onClick),
        ) {
            // Photo-forward header — fills the card width. Faint initial shows
            // behind it while loading / if there's no photo.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(SurfaceContainerLow),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = provider.name.firstOrNull()?.toString() ?: "؟",
                    color = Primary.copy(alpha = 0.35f),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                )
                val url = HomeImageAssets.imageUrl(provider.photoUrl)
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = provider.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                // Floating rating badge (top-end = top-left in RTL).
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .shadow(3.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(text = "★", color = StarYellow, fontSize = 11.sp)
                    Text(
                        text = formatRating(provider.rating),
                        color = OnSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            // Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = provider.name,
                    color = OnSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = provider.profession,
                    color = OnSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (provider.reviewsCount > 0) {
                    Text(
                        text = "${provider.reviewsCount} تقييم",
                        color = OnSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderGridCardSkeleton(index: Int) {
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
                    .aspectRatio(1f)
                    .background(SurfaceContainerHigh),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(10.dp)
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

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "ما في مزودين متاحين حالياً",
            color = OnSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatRating(rating: Double): String {
    if (rating == 0.0) return "—"
    val rounded = (rating * 10).toInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}
