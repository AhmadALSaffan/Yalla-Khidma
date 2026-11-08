package com.yallakhedma.app.presentation.screens.explore

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.LocalBottomBarInset
import com.yallakhedma.app.presentation.components.StaggeredItem
import com.yallakhedma.app.presentation.components.YkChip
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkSearchField
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.i18n.displayName
import com.yallakhedma.app.presentation.screens.common.ProviderListCard
import com.yallakhedma.app.presentation.screens.common.ServiceListCard
import com.yallakhedma.app.presentation.screens.common.ServiceListCardSkeleton
import com.yallakhedma.app.presentation.screens.main.LocalInTabs
import com.yallakhedma.app.presentation.screens.providers.ProviderDetailsScreen
import com.yallakhedma.app.presentation.screens.services.ServiceDetailsScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.explore_all
import yallakhidma.shared.generated.resources.explore_empty_body
import yallakhidma.shared.generated.resources.explore_empty_title
import yallakhidma.shared.generated.resources.explore_results
import yallakhidma.shared.generated.resources.explore_search_placeholder
import yallakhidma.shared.generated.resources.explore_tab_providers
import yallakhidma.shared.generated.resources.explore_tab_services
import yallakhidma.shared.generated.resources.explore_title

/**
 * Search + browse. Used as the client's Explore tab and pushed standalone for
 * "See all" / category taps (then it shows a back button).
 */
data class ExploreScreen(
    private val initialMode: ExploreMode = ExploreMode.Services,
    private val initialCategoryId: String? = null,
) : Screen {

    // Unique per instance: the tab copy and pushed copies must not share a ScreenModel.
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ExploreScreenModel>()
        val state by model.state.collectAsState()
        val inTabs = LocalInTabs.current
        val language = LocalAppLanguage.current
        val bottomInset = LocalBottomBarInset.current

        LaunchedEffect(Unit) { model.init(initialMode, initialCategoryId) }

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding(),
        ) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                if (inTabs) {
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(Res.string.explore_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                } else {
                    YkTopBar(
                        onBack = { navigator.pop() },
                        title = stringResource(Res.string.explore_title),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                YkSearchField(
                    value = state.query,
                    onValueChange = model::setQuery,
                    placeholder = stringResource(Res.string.explore_search_placeholder),
                )
                Spacer(Modifier.height(14.dp))
                ModeToggle(state.mode, model::setMode)
            }

            // Category filter rail
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                YkChip(stringResource(Res.string.explore_all), state.categoryId == null, { model.setCategory(null) })
                state.categories.forEach { c ->
                    YkChip(c.displayName(language), state.categoryId == c.id, { model.setCategory(c.id) })
                }
            }

            val services = model.sortedServices(state)
            val providers = state.providers
            val count = if (state.mode == ExploreMode.Services) services.size else providers.size

            AnimatedContent(
                targetState = state.mode,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(140)) },
                modifier = Modifier.weight(1f),
                label = "explore-mode",
            ) { mode ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = bottomInset + 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val loading = if (mode == ExploreMode.Services) state.servicesLoading else state.providersLoading
                    when {
                        loading -> items(4) { ServiceListCardSkeleton() }
                        count == 0 -> item {
                            EmptyState(
                                icon = YkIcons.Search,
                                title = stringResource(Res.string.explore_empty_title),
                                body = stringResource(Res.string.explore_empty_body),
                            )
                        }
                        else -> {
                            item {
                                Text(
                                    stringResource(Res.string.explore_results, count),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Yk.Ink3,
                                )
                            }
                            if (mode == ExploreMode.Services) {
                                itemsIndexed(services, key = { _, hit -> "s-" + hit.service.id }) { i, hit ->
                                    StaggeredItem(i) {
                                        ServiceListCard(
                                            service = hit.service,
                                            distanceKm = hit.distanceKm,
                                            onClick = { navigator.push(ServiceDetailsScreen(hit.service)) },
                                        )
                                    }
                                }
                            } else {
                                itemsIndexed(providers, key = { _, p -> "p-" + p.id }) { i, p ->
                                    StaggeredItem(i) {
                                        ProviderListCard(p, onClick = { navigator.push(ProviderDetailsScreen(p)) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!inTabs) Spacer(Modifier.navigationBarsPadding())
        }
    }
}

/** Two-segment toggle with a sliding thumb. */
@Composable
private fun ModeToggle(mode: ExploreMode, onChange: (ExploreMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(YkRadius.pill)
            .background(Yk.Fill)
            .padding(4.dp),
    ) {
        listOf(
            ExploreMode.Services to stringResource(Res.string.explore_tab_services),
            ExploreMode.Providers to stringResource(Res.string.explore_tab_providers),
        ).forEach { (m, label) ->
            val selected = m == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(YkRadius.pill)
                    .background(if (selected) Yk.Canvas else Color.Transparent)
                    .clickable(role = Role.Tab) { onChange(m) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) Yk.Ink else Yk.Ink3,
                )
            }
        }
    }
}
