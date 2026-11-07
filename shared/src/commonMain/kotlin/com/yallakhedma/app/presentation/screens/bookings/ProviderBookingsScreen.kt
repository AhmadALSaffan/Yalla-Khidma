package com.yallakhedma.app.presentation.screens.bookings

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.LocalBottomBarInset
import com.yallakhedma.app.presentation.components.YkChip
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.screens.common.ProviderRequestCard
import com.yallakhedma.app.presentation.screens.main.LocalInTabs
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.bookings_filter_accepted
import yallakhidma.shared.generated.resources.bookings_filter_all
import yallakhidma.shared.generated.resources.bookings_filter_paid
import yallakhidma.shared.generated.resources.bookings_filter_pending
import yallakhidma.shared.generated.resources.requests_empty_body
import yallakhidma.shared.generated.resources.requests_empty_title
import yallakhidma.shared.generated.resources.requests_filter_rejected
import yallakhidma.shared.generated.resources.requests_subtitle
import yallakhidma.shared.generated.resources.requests_title

private val FilterLabels: Map<ProviderBookingsScreenModel.Filter, StringResource> = mapOf(
    ProviderBookingsScreenModel.Filter.All to Res.string.bookings_filter_all,
    ProviderBookingsScreenModel.Filter.Pending to Res.string.bookings_filter_pending,
    ProviderBookingsScreenModel.Filter.Accepted to Res.string.bookings_filter_accepted,
    ProviderBookingsScreenModel.Filter.Paid to Res.string.bookings_filter_paid,
    ProviderBookingsScreenModel.Filter.Rejected to Res.string.requests_filter_rejected,
)

/** Every booking request the provider received (provider "Requests" tab). */
object ProviderBookingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<ProviderBookingsScreenModel>()
        val state by model.state.collectAsState()
        val inTabs = LocalInTabs.current

        Column(Modifier.fillMaxSize().background(Yk.Canvas).safeTopPadding()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                if (!inTabs) YkTopBar(onBack = { navigator.pop() }, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.requests_title), style = MaterialTheme.typography.displaySmall, color = Yk.Ink)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(Res.string.requests_subtitle), style = MaterialTheme.typography.bodyLarge, color = Yk.Ink2)
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProviderBookingsScreenModel.Filter.entries.forEach { f ->
                    val count = state.counts[f] ?: 0
                    YkChip(
                        label = "${stringResource(FilterLabels.getValue(f))}  $count",
                        selected = state.filter == f,
                        onClick = { model.setFilter(f) },
                    )
                }
            }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = LocalBottomBarInset.current + 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    state.loading -> items(4) { Box(Modifier.fillMaxWidth().height(150.dp).shimmer(YkRadius.card)) }
                    state.filtered.isEmpty() -> item {
                        EmptyState(
                            icon = YkIcons.Clipboard,
                            title = stringResource(Res.string.requests_empty_title),
                            body = stringResource(Res.string.requests_empty_body),
                        )
                    }
                    else -> items(state.filtered, key = { it.id }) { req ->
                        ProviderRequestCard(
                            request = req,
                            onAccept = { model.accept(req.id) },
                            onReject = { model.reject(req.id) },
                            modifier = Modifier.animateItem(fadeInSpec = tween(240), fadeOutSpec = tween(160), placementSpec = tween(300)),
                        )
                    }
                }
            }
        }
    }
}
