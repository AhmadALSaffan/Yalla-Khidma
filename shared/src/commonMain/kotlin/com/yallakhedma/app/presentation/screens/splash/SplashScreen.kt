package com.yallakhedma.app.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import com.yallakhedma.app.data.local.AppPreferences
import com.yallakhedma.app.presentation.components.BrandMark
import com.yallakhedma.app.presentation.screens.onboarding.OnboardingScreen
import com.yallakhedma.app.presentation.theme.Yk
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.app_name
import yallakhidma.shared.generated.resources.app_tagline
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res

object SplashScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = koinInject<AuthRepository>()
        val categoryRepository = koinInject<CategoryRepository>()
        val platformContext = LocalPlatformContext.current
        val prefs = koinInject<AppPreferences>()

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
            // Signed-out users go to onboarding/welcome, which shows none of
            // these images — don't hold them on the splash.
            if (user != null) withTimeoutOrNull(PRELOAD_TIMEOUT_MS) { preloadJobs.joinAll() }

            navigator.replaceAll(
                if (user == null && !prefs.onboardingSeen) OnboardingScreen else destinationForUser(user),
            )
        }

        SplashContent()
    }

    private const val SPLASH_DURATION_MS = 2000L
    private const val SESSION_HYDRATION_TIMEOUT_MS = 3000L
    private const val PRELOAD_TIMEOUT_MS = 4000L
    private const val CATEGORY_FETCH_TIMEOUT_MS = 2500L
}

@Composable
private fun SplashContent() {
    // Entrance choreography: mark springs in → wordmark rises → tagline fades → loader.
    val mark = remember { Animatable(0f) }
    val words = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { mark.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 220f)) }
        delay(260)
        words.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
    }
    val glow = rememberInfiniteTransition(label = "splash-glow")
    val pulse by glow.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val sweep by glow.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
        label = "sweep",
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Yk.Canvas),
        contentAlignment = Alignment.Center,
    ) {
        // Soft brand glow behind the mark.
        Box(
            modifier = Modifier
                .size(260.dp)
                .graphicsLayer { scaleX = pulse * mark.value; scaleY = pulse * mark.value; alpha = 0.9f }
                .background(
                    Brush.radialGradient(listOf(Yk.OrangeTint, Yk.OrangeSoft.copy(alpha = 0.4f), Color.Transparent)),
                    CircleShape,
                ),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 0.6f + 0.4f * mark.value
                        scaleY = 0.6f + 0.4f * mark.value
                        alpha = mark.value.coerceIn(0f, 1f)
                    }
                    .size(136.dp)
                    .shadow(20.dp, CircleShape, ambientColor = Yk.Orange.copy(alpha = 0.4f), spotColor = Yk.Orange.copy(alpha = 0.4f))
                    .background(Color.White, CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) { BrandMark(size = 104.dp) }
            Spacer(Modifier.height(20.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = words.value
                    translationY = (1f - words.value) * 24.dp.toPx()
                },
            ) {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    color = Yk.Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.app_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Yk.Ink2,
                )
            }
        }
        // Indeterminate brand loader at the bottom.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 56.dp)
                .width(72.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Yk.OrangeSoft)
                .graphicsLayer { alpha = words.value },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(28.dp)
                    .graphicsLayer { translationX = (sweep * 100.dp.toPx()) - 28.dp.toPx() }
                    .clip(CircleShape)
                    .background(Yk.Orange),
            )
        }
    }
}
