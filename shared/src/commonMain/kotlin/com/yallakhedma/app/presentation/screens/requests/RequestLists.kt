package com.yallakhedma.app.presentation.screens.requests

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.yallakhedma.app.domain.model.OpenRequest
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkChip
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.shimmer
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.i18n.displayName
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.chats_all
import yallakhidma.shared.generated.resources.home_post_cta
import yallakhidma.shared.generated.resources.jobs_empty_body
import yallakhidma.shared.generated.resources.jobs_empty_title
import yallakhidma.shared.generated.resources.oreq_empty_body
import yallakhidma.shared.generated.resources.oreq_empty_title

/** Two-option pill switch with a sliding thumb (RTL-aware). */
@Composable
fun SegmentedTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier.fillMaxWidth().height(46.dp).clip(YkRadius.pill).background(Yk.Fill).padding(4.dp),
    ) {
        val seg = maxWidth / labels.size
        val offset by animateDpAsState(seg * selected, spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow), label = "seg")
        Box(
            Modifier.offset(x = offset).width(seg).fillMaxHeight()
                .shadow(4.dp, YkRadius.pill, ambientColor = Yk.Ink.copy(alpha = 0.1f), spotColor = Yk.Ink.copy(alpha = 0.1f))
                .clip(YkRadius.pill).background(Yk.Canvas),
        )
        Row(Modifier.fillMaxSize()) {
            labels.forEachIndexed { i, label ->
                val color by animateColorAsState(if (i == selected) Yk.Ink else Yk.Ink3, tween(180), label = "seg-text")
                Box(
                    Modifier.weight(1f).fillMaxHeight().clip(YkRadius.pill).clickable(role = Role.Tab) { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) { Text(label, style = MaterialTheme.typography.titleSmall, color = color) }
            }
        }
    }
}

/** Client: their open requests (inside the Bookings tab). */
@Composable
fun MyRequestsList(
    state: MyRequestsScreenModel.State,
    onOpen: (OpenRequest) -> Unit,
    onNew: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            YkButton(
                text = stringResource(Res.string.home_post_cta),
                onClick = onNew,
                height = 50.dp,
                leading = { Icon(YkIcons.Plus, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
            )
        }
        when {
            state.loading -> items(3) { Box(Modifier.fillMaxWidth().height(140.dp).shimmer(YkRadius.card)) }
            state.requests.isEmpty() -> item {
                EmptyState(YkIcons.Clipboard, stringResource(Res.string.oreq_empty_title), body = stringResource(Res.string.oreq_empty_body))
            }
            else -> items(state.requests, key = { it.id }) { r ->
                OpenRequestCard(
                    request = r,
                    onClick = { onOpen(r) },
                    modifier = Modifier.animateItem(fadeInSpec = tween(240), fadeOutSpec = tween(160), placementSpec = tween(300)),
                )
            }
        }
    }
}

/** Provider: open jobs feed with category filter (inside the Requests tab). */
@Composable
fun OpenJobsList(
    state: OpenJobsScreenModel.State,
    onCategory: (String?) -> Unit,
    onOpen: (OpenRequest) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    androidx.compose.foundation.layout.Column(modifier.fillMaxSize()) {
        if (state.categories.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                YkChip(stringResource(Res.string.chats_all), state.categoryId == null, { onCategory(null) })
                state.categories.forEach { c -> YkChip(c.displayName(language), state.categoryId == c.id, { onCategory(c.id) }) }
            }
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val list = state.visible
            when {
                state.loading -> items(3) { Box(Modifier.fillMaxWidth().height(140.dp).shimmer(YkRadius.card)) }
                list.isEmpty() -> item {
                    EmptyState(YkIcons.Briefcase, stringResource(Res.string.jobs_empty_title), body = stringResource(Res.string.jobs_empty_body))
                }
                else -> items(list, key = { it.id }) { r ->
                    OpenRequestCard(
                        request = r,
                        onClick = { onOpen(r) },
                        providerView = true,
                        myOffer = state.myOffers[r.id],
                        modifier = Modifier.animateItem(fadeInSpec = tween(240), fadeOutSpec = tween(160), placementSpec = tween(300)),
                    )
                }
            }
        }
    }
}
