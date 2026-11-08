package com.yallakhedma.app.presentation.screens.categories

import com.yallakhedma.app.presentation.components.safeTopPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.RemoteImage
import com.yallakhedma.app.presentation.components.StaggeredItem
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.pressable
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.components.ykCard
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.i18n.displayName
import com.yallakhedma.app.presentation.screens.explore.ExploreScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.categories_empty
import yallakhidma.shared.generated.resources.categories_subtitle
import yallakhidma.shared.generated.resources.categories_title

/** All categories from Firestore; tapping one opens Explore filtered to it. */
object AllCategoriesScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<AllCategoriesScreenModel>()
        val state by model.state.collectAsState()
        val language = LocalAppLanguage.current

        Column(
            Modifier
                .fillMaxSize()
                .background(Yk.Canvas)
                .safeTopPadding(),
        ) {
            YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                        Text(stringResource(Res.string.categories_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(Res.string.categories_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
                    }
                }
                when {
                    state.loading && state.categories.isEmpty() -> items(9) {
                        Box(Modifier.fillMaxWidth().aspectRatio(0.82f).shimmer(YkRadius.card))
                    }
                    state.categories.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(YkIcons.Grid, stringResource(Res.string.categories_empty))
                    }
                    else -> itemsIndexed(state.categories.sortedBy { it.order }, key = { _, c -> c.id }) { i, category ->
                        val label = category.displayName(language)
                        StaggeredItem(i) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .pressable({ navigator.push(ExploreScreen(initialCategoryId = category.id)) })
                                    .ykCard(elevation = 6.dp)
                                    .padding(10.dp),
                            ) {
                                RemoteImage(
                                    category.imagePath,
                                    label,
                                    Modifier.fillMaxWidth().aspectRatio(1f).clip(YkRadius.button),
                                ) { Icon(YkIcons.Grid, null, tint = Yk.Orange.copy(alpha = 0.6f), modifier = Modifier.size(28.dp)) }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Yk.Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}
