package com.yallakhedma.app.presentation.screens.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.LocalBottomBarInset
import com.yallakhedma.app.presentation.components.StaggeredItem
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.screens.common.ServiceListCard
import com.yallakhedma.app.presentation.screens.main.LocalInTabs
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.services_add
import yallakhidma.shared.generated.resources.services_empty_body
import yallakhidma.shared.generated.resources.services_empty_title
import yallakhidma.shared.generated.resources.services_subtitle
import yallakhidma.shared.generated.resources.services_title

/** The provider's published services (provider "My services" tab). */
object MyServicesScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<MyServicesScreenModel>()
        val state by model.state.collectAsState()
        val inTabs = LocalInTabs.current

        Column(Modifier.fillMaxSize().background(Yk.Canvas).safeTopPadding()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                if (!inTabs) YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(Res.string.services_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(Res.string.services_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
                    }
                }
                Spacer(Modifier.height(16.dp))
                YkButton(
                    text = stringResource(Res.string.services_add),
                    onClick = { navigator.push(AddServiceScreen) },
                    height = 50.dp,
                    leading = { Icon(YkIcons.Plus, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                )
                Spacer(Modifier.height(16.dp))
            }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = LocalBottomBarInset.current + 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    state.loading -> items(3) { Box(Modifier.fillMaxWidth().height(116.dp).shimmer(YkRadius.card)) }
                    state.services.isEmpty() -> item {
                        EmptyState(
                            icon = YkIcons.Briefcase,
                            title = stringResource(Res.string.services_empty_title),
                            body = stringResource(Res.string.services_empty_body),
                        )
                    }
                    else -> itemsIndexed(state.services, key = { _, s -> s.id }) { i, svc ->
                        StaggeredItem(i) {
                            ServiceListCard(
                                service = svc,
                                onClick = { navigator.push(ServiceDetailsScreen(svc)) },
                            )
                        }
                    }
                }
            }
        }
    }
}
