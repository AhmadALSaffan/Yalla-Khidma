package com.yallakhedma.app.presentation.screens.home

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.ServiceCategory
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.components.Avatar
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.LocalBottomBarInset
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.SectionHeader
import com.yallakhedma.app.presentation.components.StaggeredItem
import com.yallakhedma.app.presentation.components.YkIconButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkSearchField
import com.yallakhedma.app.presentation.components.pressable
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.i18n.displayName
import com.yallakhedma.app.presentation.screens.categories.AllCategoriesScreen
import com.yallakhedma.app.presentation.screens.common.ProviderRailCard
import com.yallakhedma.app.presentation.screens.common.ProviderRailCardSkeleton
import com.yallakhedma.app.presentation.screens.common.ServiceListCard
import com.yallakhedma.app.presentation.screens.common.ServiceListCardSkeleton
import com.yallakhedma.app.presentation.screens.explore.ExploreMode
import com.yallakhedma.app.presentation.screens.explore.ExploreScreen
import com.yallakhedma.app.presentation.screens.explore.withDistances
import com.yallakhedma.app.presentation.screens.main.ClientMainScreen
import com.yallakhedma.app.presentation.screens.main.LocalTabController
import com.yallakhedma.app.presentation.screens.providers.ProviderDetailsScreen
import com.yallakhedma.app.presentation.screens.services.ServiceDetailsScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.ClientLocationHolder
import com.yallakhedma.app.util.rememberLocationPicker
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.cd_notifications
import yallakhidma.shared.generated.resources.home_banner_code
import yallakhidma.shared.generated.resources.home_banner_cta
import yallakhidma.shared.generated.resources.home_banner_title
import yallakhidma.shared.generated.resources.home_categories
import yallakhidma.shared.generated.resources.home_empty_providers
import yallakhidma.shared.generated.resources.home_empty_services
import yallakhidma.shared.generated.resources.home_featured
import yallakhidma.shared.generated.resources.home_greeting_evening
import yallakhidma.shared.generated.resources.home_greeting_guest
import yallakhidma.shared.generated.resources.home_greeting_morning
import yallakhidma.shared.generated.resources.home_greeting_name
import yallakhidma.shared.generated.resources.home_location_pick
import yallakhidma.shared.generated.resources.home_nearby
import yallakhidma.shared.generated.resources.home_search_placeholder

/** Client Home (tab content of [ClientMainScreen]). */
object ClientHomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val tabs = LocalTabController.current
        val auth = koinInject<AuthRepository>()
        val locationHolder = koinInject<ClientLocationHolder>()
        val model = koinScreenModel<ClientHomeScreenModel>()
        val state by model.state.collectAsState()
        val user by auth.currentUser.collectAsState(initial = null)
        val location by locationHolder.location.collectAsState()
        val pickLocation = rememberLocationPicker { picked -> if (picked != null) locationHolder.set(picked) }
        val language = LocalAppLanguage.current
        val bottomInset = LocalBottomBarInset.current
        val openExplore: () -> Unit = { tabs?.select(ClientMainScreen.TAB_EXPLORE) ?: navigator.push(ExploreScreen()) }

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .verticalScroll(rememberScrollState())
                .safeTopPadding()
                .padding(bottom = bottomInset + 16.dp),
        ) {
            // ── Header ─────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Avatar(
                    name = user?.name.orEmpty(),
                    photo = user?.photoUrl,
                    size = 48.dp,
                    modifier = Modifier.clickable(role = Role.Button) { tabs?.select(ClientMainScreen.TAB_PROFILE) },
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(if (isMorning()) Res.string.home_greeting_morning else Res.string.home_greeting_evening),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Yk.Ink3,
                    )
                    val name = user?.name?.substringBefore(' ')?.takeIf { it.isNotBlank() }
                    Text(
                        if (name != null) stringResource(Res.string.home_greeting_name, name)
                        else stringResource(Res.string.home_greeting_guest),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Yk.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                YkIconButton(
                    icon = YkIcons.Bell,
                    contentDescription = stringResource(Res.string.cd_notifications),
                    onClick = {},
                    bordered = true,
                    background = Yk.Canvas,
                )
            }

            // Location chip
            Row(
                Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp)
                    .clip(YkRadius.pill)
                    .background(Yk.OrangeSoft)
                    .clickable(role = Role.Button) { pickLocation() }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(YkIcons.MapPin, null, tint = Yk.Orange, modifier = Modifier.size(16.dp))
                Text(
                    location?.label ?: stringResource(Res.string.home_location_pick),
                    style = MaterialTheme.typography.labelLarge,
                    color = Yk.OrangeDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(18.dp))
            YkSearchField(
                value = "",
                onValueChange = {},
                placeholder = stringResource(Res.string.home_search_placeholder),
                onClick = openExplore,
                elevated = true,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(22.dp))
            PromoBanner(onClick = openExplore, modifier = Modifier.padding(horizontal = 20.dp))

            // ── Categories ────────────────────────────────────────────────
            Spacer(Modifier.height(28.dp))
            SectionHeader(
                stringResource(Res.string.home_categories),
                onSeeAll = { navigator.push(AllCategoriesScreen) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (state.categoriesLoading && state.categories.isEmpty()) {
                    repeat(5) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(68.dp).shimmer(YkRadius.card))
                            Spacer(Modifier.height(8.dp))
                            Box(Modifier.width(48.dp).height(10.dp).shimmer())
                        }
                    }
                } else {
                    state.categories.forEach { category ->
                        CategoryTile(
                            category = category,
                            label = category.displayName(language),
                            onClick = { navigator.push(ExploreScreen(initialCategoryId = category.id)) },
                        )
                    }
                }
            }

            // ── Top rated pros ────────────────────────────────────────────
            Spacer(Modifier.height(28.dp))
            SectionHeader(
                stringResource(Res.string.home_featured),
                onSeeAll = { navigator.push(ExploreScreen(initialMode = ExploreMode.Providers)) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(12.dp))
            when {
                state.providersLoading && state.providers.isEmpty() -> Row(
                    Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) { repeat(3) { ProviderRailCardSkeleton() } }
                state.providers.isEmpty() -> EmptyState(YkIcons.User, stringResource(Res.string.home_empty_providers))
                else -> Row(
                    Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    state.providers.forEachIndexed { i, p ->
                        StaggeredItem(i) { ProviderRailCard(p, onClick = { navigator.push(ProviderDetailsScreen(p)) }) }
                    }
                }
            }

            // ── Near you ─────────────────────────────────────────────────
            Spacer(Modifier.height(20.dp))
            SectionHeader(
                stringResource(Res.string.home_nearby),
                onSeeAll = { navigator.push(ExploreScreen(initialMode = ExploreMode.Services)) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(12.dp))
            Column(
                Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val hits = remember(state.services, location) { state.services.withDistances(location) }
                when {
                    state.servicesLoading && state.services.isEmpty() -> repeat(3) { ServiceListCardSkeleton() }
                    hits.isEmpty() -> EmptyState(YkIcons.MapPin, stringResource(Res.string.home_empty_services))
                    else -> hits.forEachIndexed { i, hit ->
                        StaggeredItem(i) {
                            ServiceListCard(
                                service = hit.service,
                                distanceKm = hit.distanceKm,
                                onClick = { navigator.push(ServiceDetailsScreen(hit.service)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isMorning(): Boolean =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour < 12

@Composable
private fun PromoBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 164.dp)
            .shadow(16.dp, YkRadius.hero, ambientColor = Yk.Orange.copy(alpha = 0.35f), spotColor = Yk.Orange.copy(alpha = 0.35f))
            .clip(YkRadius.hero)
            .pressable(onClick),
    ) {
        RemoteImage(HomeImageAssets.BANNER, null, Modifier.matchParentSize(), placeholder = Yk.Orange)
        // Solid brand color on the text side, fading toward the photo.
        val fade = listOf(Yk.OrangeDeep.copy(alpha = 0.96f), Yk.Orange.copy(alpha = 0.85f), Color.Transparent)
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(if (rtl) fade.reversed() else fade),
            ),
        )
        Column(
            Modifier.fillMaxWidth(0.72f).align(Alignment.CenterStart).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(stringResource(Res.string.home_banner_title), style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text(stringResource(Res.string.home_banner_code), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(Res.string.home_banner_cta),
                style = MaterialTheme.typography.labelLarge,
                color = Yk.OrangeDeep,
                modifier = Modifier.clip(YkRadius.pill).background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun CategoryTile(category: ServiceCategory, label: String, onClick: () -> Unit) {
    Column(
        Modifier.width(76.dp).pressable(onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RemoteImage(
            category.imagePath,
            label,
            Modifier.size(68.dp).clip(YkRadius.card),
            placeholder = Yk.OrangeSoft,
        ) { Icon(YkIcons.Grid, null, tint = Yk.Orange.copy(alpha = 0.6f), modifier = Modifier.size(26.dp)) }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = Yk.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
