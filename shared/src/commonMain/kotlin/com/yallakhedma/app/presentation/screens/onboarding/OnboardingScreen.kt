package com.yallakhedma.app.presentation.screens.onboarding

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.data.local.AppPreferences
import com.yallakhedma.app.presentation.components.BrandMark
import com.yallakhedma.app.presentation.components.LanguageSwitch
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.screens.auth.welcome.WelcomeScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.app_name
import yallakhidma.shared.generated.resources.common_next
import yallakhidma.shared.generated.resources.common_skip
import yallakhidma.shared.generated.resources.onboarding_1_body
import yallakhidma.shared.generated.resources.onboarding_1_title
import yallakhidma.shared.generated.resources.onboarding_2_body
import yallakhidma.shared.generated.resources.onboarding_2_title
import yallakhidma.shared.generated.resources.onboarding_3_body
import yallakhidma.shared.generated.resources.onboarding_3_title
import yallakhidma.shared.generated.resources.onboarding_get_started
import yallakhidma.shared.generated.resources.onboarding_step_cd

private data class OnboardingStep(
    val title: StringResource,
    val body: StringResource,
    val scene: @Composable (Float) -> Unit,
)

private val Steps = listOf(
    OnboardingStep(Res.string.onboarding_1_title, Res.string.onboarding_1_body) { SearchScene(it) },
    OnboardingStep(Res.string.onboarding_2_title, Res.string.onboarding_2_body) { TrustScene(it) },
    OnboardingStep(Res.string.onboarding_3_title, Res.string.onboarding_3_body) { DoneScene(it) },
)

/** Length of each step's motion clip; the story bar fills over this time. */
private const val STEP_DURATION_MS = 5600

/**
 * Story-style onboarding: each step is a short motion clip synced to a
 * segmented progress bar. Steps auto-advance when their clip ends; users can
 * swipe, tap Next, or skip. Seen once — the flag lives in [AppPreferences].
 */
object OnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val prefs = koinInject<AppPreferences>()
        val scope = rememberCoroutineScope()
        val pager = rememberPagerState { Steps.size }
        val clip = remember { Animatable(0f) }
        val last = Steps.lastIndex

        fun finish() {
            prefs.onboardingSeen = true
            navigator.replaceAll(WelcomeScreen)
        }

        // Play the settled step's clip; when it ends, advance to the next one.
        // Keyed on settledPage (not currentPage, which flips mid-scroll) and the
        // scroll runs in its own scope so restarting this effect can't cancel it.
        LaunchedEffect(pager.settledPage) {
            clip.snapTo(0f)
            clip.animateTo(1f, tween(STEP_DURATION_MS, easing = LinearEasing))
            val page = pager.settledPage
            if (page < last) scope.launch { pager.animateScrollToPage(page + 1) }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            StoryBars(current = pager.settledPage, progress = clip.value, count = Steps.size)
            Spacer(Modifier.height(16.dp))

            // Header: brand · language · skip
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BrandMark(size = 32.dp)
                Spacer(Modifier.padding(4.dp))
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.titleMedium, color = Yk.Ink)
                Spacer(Modifier.weight(1f))
                LanguageSwitch()
                if (pager.currentPage < last) {
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        text = stringResource(Res.string.common_skip),
                        style = MaterialTheme.typography.titleSmall,
                        color = Yk.Ink2,
                        modifier = Modifier
                            .clip(YkRadius.chip)
                            .clickable { finish() }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            // The "video" — swipeable stage with a warm backdrop.
            val stepLabel = stringResource(Res.string.onboarding_step_cd, pager.currentPage + 1, Steps.size)
            HorizontalPager(
                state = pager,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(YkRadius.hero)
                    .background(Yk.WarmGlow)
                    .semantics { contentDescription = stepLabel },
            ) { page ->
                val t = when {
                    page < pager.settledPage -> 1f
                    page > pager.settledPage -> 0f
                    else -> clip.value
                }
                Box(Modifier.fillMaxSize().padding(12.dp)) { Steps[page].scene(t) }
            }

            Spacer(Modifier.height(28.dp))
            AnimatedContent(
                targetState = pager.currentPage,
                transitionSpec = {
                    (fadeIn(tween(320, delayMillis = 80)) + slideInVertically(tween(380)) { it / 3 }) togetherWith
                        (fadeOut(tween(160)) + slideOutVertically(tween(200)) { -it / 4 })
                },
                label = "onboarding-copy",
            ) { page ->
                Column(Modifier.fillMaxWidth().height(132.dp)) {
                    Text(
                        text = stringResource(Steps[page].title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Yk.Ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(Steps[page].body),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Yk.Ink2,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            val isLast = pager.currentPage == last
            YkButton(
                text = stringResource(if (isLast) Res.string.onboarding_get_started else Res.string.common_next),
                onClick = {
                    if (isLast) finish()
                    else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StoryBars(current: Int, progress: Float, count: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { i ->
            val fill = when {
                i < current -> 1f
                i == current -> progress
                else -> 0f
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(YkRadius.pill)
                    .background(Yk.Line),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fill)
                        .background(Yk.Orange),
                )
            }
        }
    }
}
