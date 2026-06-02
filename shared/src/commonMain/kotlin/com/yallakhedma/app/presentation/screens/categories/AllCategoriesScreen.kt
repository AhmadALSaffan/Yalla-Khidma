package com.yallakhedma.app.presentation.screens.categories

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.yallakhedma.app.domain.model.ServiceCategory
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets

// Stitch palette (consistent with the home screen).
private val Background = Color(0xFFFFFFFF)
private val SurfaceContainer = Color(0xFFEAEDFF)
private val SurfaceContainerHigh = Color(0xFFE2E7FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFA73A00)
private val Secondary = Color(0xFF5D3FD3)
private val Tertiary = Color(0xFF00696E)

private val tints = listOf(Primary, Secondary, Tertiary)

object AllCategoriesScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AllCategoriesScreenModel>()
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
                    state.loading && state.categories.isEmpty() -> CategoryGrid(
                        count = 9,
                    ) { index, _ -> CategoryTileSkeleton(index) }
                    state.categories.isEmpty() -> EmptyState()
                    else -> CategoryGrid(
                        count = state.categories.size,
                    ) { index, _ ->
                        CategoryTile(
                            category = state.categories[index],
                            tint = tints[index % tints.size],
                            index = index,
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
                text = "كل الخدمات",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.size(40.dp))
        }
        Text(
            text = "تصفح كل فئات الخدمات المتوفرة",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun CategoryGrid(
    count: Int,
    cell: @Composable (index: Int, columnWeight: Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        (0 until count).chunked(3).forEach { rowIndices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                rowIndices.forEach { index ->
                    Box(modifier = Modifier.weight(1f)) { cell(index, 1f) }
                }
                // Pad the last row so tiles keep their column width.
                repeat(3 - rowIndices.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryTile(category: ServiceCategory, tint: Color, index: Int) {
    StaggeredEntrance(index) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Big square tile — fills the column width so the 3 tiles per row
            // look large and prominent.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                // Letter fallback shows behind the image (and if it fails).
                Text(
                    text = category.nameAr.firstOrNull()?.toString() ?: "؟",
                    color = tint,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
                val url = HomeImageAssets.imageUrl(category.imagePath)
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = category.nameAr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onState = { st ->
                            if (st is AsyncImagePainter.State.Error) {
                                println(
                                    "[YallaKhedma][Categories] FAILED '${category.nameAr}' " +
                                        "path='${category.imagePath}' url='$url' — ${st.result.throwable}",
                                )
                            }
                        },
                    )
                } else {
                    println("[YallaKhedma][Categories] '${category.nameAr}' has empty imagePath")
                }
            }
            Text(
                text = category.nameAr,
                color = OnSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun CategoryTileSkeleton(index: Int) {
    StaggeredEntrance(index) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceContainerHigh),
            )
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceContainerHigh),
            )
        }
    }
}

/** Fades + lifts each tile into place with a per-index delay for a staggered feel. */
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
            text = "ما في فئات متاحة حالياً",
            color = OnSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}
