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
import com.yallakhedma.app.presentation.screens.bookings.ProviderBookingsScreen
import com.yallakhedma.app.presentation.screens.dashboard.ProviderDashboardScreen
import com.yallakhedma.app.presentation.screens.profile.ProviderProfileScreen
import com.yallakhedma.app.presentation.screens.services.MyServicesScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.nav_dashboard
import yallakhidma.shared.generated.resources.nav_profile
import yallakhidma.shared.generated.resources.nav_requests
import yallakhidma.shared.generated.resources.nav_services

// File-level (not on the Screen object, which Voyager Java-serializes).
private val providerSelectedTab = mutableIntStateOf(ProviderMainScreen.TAB_DASHBOARD)

/** Provider app shell: Dashboard · Requests · My services · Profile. */
object ProviderMainScreen : Screen {

    const val TAB_DASHBOARD = 0
    const val TAB_REQUESTS = 1
    const val TAB_SERVICES = 2
    const val TAB_PROFILE = 3

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val auth = koinInject<AuthRepository>()
        val user by auth.currentUser.collectAsState(initial = null)

        var hasSeenUser by remember { mutableStateOf(false) }
        LaunchedEffect(user) {
            if (user != null) hasSeenUser = true
            else if (hasSeenUser) {
                providerSelectedTab.intValue = TAB_DASHBOARD
                navigator.replaceAll(SignedOutDestination)
            }
        }

        MainTabsHost(
            tabs = listOf(
                MainTab(YkNavItem(YkIcons.GridFilled, stringResource(Res.string.nav_dashboard)), ProviderDashboardScreen),
                MainTab(YkNavItem(YkIcons.CalendarFilled, stringResource(Res.string.nav_requests)), ProviderBookingsScreen),
                MainTab(YkNavItem(YkIcons.BriefcaseFilled, stringResource(Res.string.nav_services)), MyServicesScreen),
                MainTab(YkNavItem(YkIcons.UserFilled, stringResource(Res.string.nav_profile)), ProviderProfileScreen),
            ),
            selectedState = providerSelectedTab,
        )
    }
}
