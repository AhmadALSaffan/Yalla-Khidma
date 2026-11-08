package com.yallakhedma.app.presentation.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkNavItem
import com.yallakhedma.app.presentation.routing.SignedOutDestination
import com.yallakhedma.app.presentation.screens.bookings.MyBookingsScreen
import com.yallakhedma.app.presentation.screens.explore.ExploreScreen
import com.yallakhedma.app.presentation.screens.home.ClientHomeScreen
import com.yallakhedma.app.presentation.screens.profile.ClientProfileScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.nav_bookings
import yallakhidma.shared.generated.resources.nav_explore
import yallakhidma.shared.generated.resources.nav_home
import yallakhidma.shared.generated.resources.nav_messages
import yallakhidma.shared.generated.resources.nav_profile

// Kept at file level (not on the Screen object, which Voyager Java-serializes
// when the app is backgrounded) — they live for the process.
private val exploreTab = ExploreScreen()
private val selectedTab = mutableIntStateOf(ClientMainScreen.TAB_HOME)

/** Client app shell: Home · Explore · Bookings · Profile. */
object ClientMainScreen : Screen {

    const val TAB_HOME = 0
    const val TAB_EXPLORE = 1
    const val TAB_BOOKINGS = 2
    const val TAB_MESSAGES = 3
    const val TAB_PROFILE = 4

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val auth = koinInject<AuthRepository>()
        val user by auth.currentUser.collectAsState(initial = null)

        // Leave once the session ends (sign-out from any tab). Guarded so the
        // initial null — before Firestore emits — doesn't bounce us out.
        var hasSeenUser by remember { mutableStateOf(false) }
        LaunchedEffect(user) {
            if (user != null) hasSeenUser = true
            else if (hasSeenUser) {
                selectedTab.intValue = TAB_HOME
                navigator.replaceAll(SignedOutDestination)
            }
        }

        MainTabsHost(
            tabs = listOf(
                MainTab(YkNavItem(YkIcons.HomeFilled, stringResource(Res.string.nav_home)), ClientHomeScreen),
                MainTab(YkNavItem(YkIcons.SearchFilled, stringResource(Res.string.nav_explore)), exploreTab),
                MainTab(YkNavItem(YkIcons.CalendarFilled, stringResource(Res.string.nav_bookings)), MyBookingsScreen),
                MainTab(YkNavItem(YkIcons.MessageFilled, stringResource(Res.string.nav_messages)), com.yallakhedma.app.presentation.screens.chats.ChatsScreen),
                MainTab(YkNavItem(YkIcons.UserFilled, stringResource(Res.string.nav_profile)), ClientProfileScreen),
            ),
            selectedState = selectedTab,
        )
    }
}
