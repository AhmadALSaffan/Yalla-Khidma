package com.yallakhedma.app.presentation.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import cafe.adriel.voyager.core.screen.Screen
import com.yallakhedma.app.presentation.components.LocalBottomBarInset
import com.yallakhedma.app.presentation.components.YkBottomBar
import com.yallakhedma.app.presentation.components.YkBottomBarDefaults
import com.yallakhedma.app.presentation.components.YkNavItem
import com.yallakhedma.app.presentation.theme.Yk

/** One tab of a [MainTabsHost]: its bar item + the screen rendered as its content. */
class MainTab(val item: YkNavItem, val screen: Screen)

/** Lets tab content switch tabs (e.g. Home's search → Explore). */
class TabController(val select: (Int) -> Unit)

val LocalTabController = staticCompositionLocalOf<TabController?> { null }

/** True when a screen is rendered as tab content (no back button, pad for the bar). */
val LocalInTabs = staticCompositionLocalOf { false }

/**
 * Hosts tab content *behind* the locked [YkBottomBar]. Each tab keeps its own
 * saved UI state (scroll position…), and switching tabs slides the content in
 * from the side that matches the direction of travel, with a fade.
 *
 * Tab screens must never also be pushed on the navigator (they'd share — and
 * then dispose — the same ScreenModels). Switch tabs via [LocalTabController].
 */
@Composable
fun MainTabsHost(tabs: List<MainTab>, selectedState: MutableIntState) {
    // State is owned by the shell screen (outlives recomposition keys such as a
    // language switch), so changing language keeps you on the same tab.
    var selected by selectedState
    val stateHolder = rememberSaveableStateHolder()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val controller = TabController { selected = it.coerceIn(0, tabs.lastIndex) }

    Box(Modifier.fillMaxSize().background(Yk.Canvas)) {
        CompositionLocalProvider(
            LocalTabController provides controller,
            LocalInTabs provides true,
            LocalBottomBarInset provides YkBottomBarDefaults.Inset,
        ) {
            AnimatedContent(
                targetState = selected,
                transitionSpec = { tabTransition(initialState, targetState, rtl) },
                modifier = Modifier.fillMaxSize(),
                label = "main-tabs",
            ) { index ->
                stateHolder.SaveableStateProvider(key = "tab-$index") {
                    tabs[index].screen.Content()
                }
            }
        }

        // Status-bar scrim so scrolled content never runs under the clock/icons.
        Box(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(Yk.Canvas.copy(alpha = 0.94f)),
        )

        YkBottomBar(
            items = tabs.map { it.item },
            selectedIndex = selected,
            onSelect = { selected = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Moving toward a tab that sits physically to the right → content enters from
 * the right (and vice versa). In RTL higher indices sit further left.
 */
private fun tabTransition(from: Int, to: Int, rtl: Boolean): ContentTransform {
    val towardRight = if (rtl) to < from else to > from
    val sign = if (towardRight) 1 else -1
    val enter = slideInHorizontally(tween(340, easing = FastOutSlowInEasing)) { w -> sign * w / 4 } +
        fadeIn(tween(260, delayMillis = 40))
    val exit = slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { w -> -sign * w / 6 } +
        fadeOut(tween(180))
    return enter togetherWith exit
}
