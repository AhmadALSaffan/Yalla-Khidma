package com.yallakhedma.app.presentation.screens.splash

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.CategoryRepository
import com.yallakhedma.app.presentation.routing.destinationForUser
import com.yallakhedma.app.presentation.screens.home.HomeImageAssets
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.icon

// Stitch splash palette (orange gradient brand screen).
private val Primary = Color(0xFFA73A00)
private val PrimaryContainer = Color(0xFFFF5C00)
private val OnPrimaryContainer = Color(0xFF521800)
private val PrimaryFixed = Color(0xFFFFDBCE)

object SplashScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = koinInject<AuthRepository>()
        val categoryRepository = koinInject<CategoryRepository>()
        val platformContext = LocalPlatformContext.current

        LaunchedEffect(Unit) {
            // Warm Coil's cache while the splash is showing, so images appear
            // instantly once we navigate.
            val imageLoader = SingletonImageLoader.get(platformContext)
            fun preload(url: String) = launch {
                runCatching {
                    imageLoader.execute(ImageRequest.Builder(platformContext).data(url).build())
                }
            }

            // Static home images (banner + the hardcoded category quick-picks).
            val preloadJobs = HomeImageAssets.preloadUrls.map { preload(it) }

            // Dynamic categories from Firestore — fetch the list, then preload
            // each image. Best-effort and time-boxed so it never stalls the
            // splash; whatever doesn't finish keeps loading in the background.
            launch {
                val categories = withTimeoutOrNull(CATEGORY_FETCH_TIMEOUT_MS) {
                    categoryRepository.observeAll().first()
                }.orEmpty()
                categories.forEach { category ->
                    HomeImageAssets.imageUrl(category.imagePath)?.let { preload(it) }
                }
            }

            delay(SPLASH_DURATION_MS)
            // If Firebase already has a persisted session, the user is logged
            // in — wait for the Firestore profile to hydrate (with a timeout
            // as a safety net). Otherwise route straight to login without
            // blocking on a flow that will only ever emit null.
            val user = if (authRepository.hasPersistedSession()) {
                withTimeoutOrNull(SESSION_HYDRATION_TIMEOUT_MS) {
                    authRepository.currentUser.firstOrNull { it != null }
                }
            } else {
                null
            }

            // Give the image preloads a brief window to finish (they've been
            // running during the splash delay already). Capped so a slow image
            // never blocks the user on the splash.
            withTimeoutOrNull(PRELOAD_TIMEOUT_MS) { preloadJobs.joinAll() }

            navigator.replaceAll(destinationForUser(user))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(colors = listOf(Primary, PrimaryContainer)),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Logo in a white circle
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon),
                        contentDescription = "شعار يلّا خِدمة",
                        modifier = Modifier.size(128.dp),
                    )
                }

                Spacer(Modifier.height(32.dp))
                Text(
                    text = "يلّا خِدمة",
                    color = OnPrimaryContainer,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(32.dp))
                BouncingDots()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "نجهز لك أفضل الخدمات...",
                    color = PrimaryFixed,
                    fontSize = 16.sp,
                )
            }
        }
    }

    private const val SPLASH_DURATION_MS = 2000L
    private const val SESSION_HYDRATION_TIMEOUT_MS = 3000L
    private const val PRELOAD_TIMEOUT_MS = 4000L
    private const val CATEGORY_FETCH_TIMEOUT_MS = 2500L
}

@Composable
private fun BouncingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val offsetY by transition.animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, delayMillis = index * 100),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$index",
            )
            Box(
                modifier = Modifier
                    .offset(y = offsetY.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(OnPrimaryContainer),
            )
        }
    }
}
