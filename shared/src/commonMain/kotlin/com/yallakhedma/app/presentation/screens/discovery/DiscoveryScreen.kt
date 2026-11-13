package com.yallakhedma.app.presentation.screens.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.discovery.AvailabilityNote
import com.yallakhedma.app.domain.discovery.DiscoveryCategory
import com.yallakhedma.app.domain.discovery.ExcludedListing
import com.yallakhedma.app.domain.discovery.ExclusionReason
import com.yallakhedma.app.domain.discovery.MatchReason
import com.yallakhedma.app.domain.discovery.ParsedQuery
import com.yallakhedma.app.domain.discovery.TimeIntent
import com.yallakhedma.app.presentation.components.EmptyState
import com.yallakhedma.app.presentation.components.StaggeredItem
import com.yallakhedma.app.presentation.components.YkButton
import com.yallakhedma.app.presentation.components.YkChip
import com.yallakhedma.app.presentation.components.YkIcons
import com.yallakhedma.app.presentation.components.YkTextField
import com.yallakhedma.app.presentation.components.YkTopBar
import com.yallakhedma.app.presentation.components.distanceLabel
import com.yallakhedma.app.presentation.components.safeTopPadding
import com.yallakhedma.app.presentation.i18n.AppLanguage
import com.yallakhedma.app.presentation.i18n.LocalAppLanguage
import com.yallakhedma.app.presentation.screens.common.ServiceListCard
import com.yallakhedma.app.presentation.screens.common.ServiceListCardSkeleton
import com.yallakhedma.app.presentation.screens.common.TagChip
import com.yallakhedma.app.presentation.screens.requests.NewRequestScreen
import com.yallakhedma.app.presentation.screens.services.ServiceDetailsScreen
import com.yallakhedma.app.presentation.theme.Yk
import com.yallakhedma.app.presentation.theme.YkRadius
import com.yallakhedma.app.util.rememberLocationPicker
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.*

/**
 * "Describe your problem" — natural-language service discovery. Results reuse
 * the marketplace service card and open the existing service → booking flow.
 */
class DiscoveryScreen(private val initialQuery: String = "") : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val model = koinScreenModel<DiscoveryScreenModel>()
        val state by model.state.collectAsState()
        val focus = LocalFocusManager.current
        val pickLocation = rememberLocationPicker(model::onLocationPicked)

        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (initialQuery.isNotBlank() && state.query.isBlank()) {
                model.setQuery(initialQuery)
                model.search()
            }
        }
        val submit: () -> Unit = { focus.clearFocus(); model.search() }

        Column(Modifier.fillMaxSize().background(Yk.Canvas).safeTopPadding()) {
            YkTopBar(
                onBack = { navigator.pop() },
                title = stringResource(Res.string.discovery_title),
                modifier = Modifier.padding(horizontal = 20.dp).padding(top = 8.dp),
            )
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SourceToggle(state.source, model::setSource)
                        if (state.source == DiscoverySource.Demo) DemoBanner()
                        LocationRow(state, onPick = pickLocation)
                        YkTextField(
                            value = state.query,
                            onValueChange = model::setQuery,
                            placeholder = stringResource(Res.string.discovery_placeholder),
                            singleLine = false,
                            imeAction = ImeAction.Search,
                            keyboardActions = KeyboardActions(onSearch = { submit() }),
                        )
                        YkButton(
                            text = stringResource(Res.string.discovery_search),
                            onClick = submit,
                            enabled = state.query.isNotBlank(),
                            loading = state.phase == DiscoveryScreenModel.Phase.Searching,
                            leading = { Icon(YkIcons.Sparkles, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (state.phase == DiscoveryScreenModel.Phase.Idle) {
                            Examples(onPick = { q -> model.setQuery(q); submit() })
                        }
                    }
                }

                when (state.phase) {
                    DiscoveryScreenModel.Phase.Idle -> Unit
                    DiscoveryScreenModel.Phase.Searching -> items(3) { ServiceListCardSkeleton() }
                    DiscoveryScreenModel.Phase.NeedsLocation -> item {
                        EmptyState(
                            icon = YkIcons.MapPin,
                            title = stringResource(Res.string.discovery_missing_location_title),
                            body = if (state.locationFailed) stringResource(Res.string.discovery_location_failed)
                            else stringResource(Res.string.discovery_missing_location_body),
                            actionLabel = stringResource(Res.string.discovery_pick_location),
                            onAction = pickLocation,
                        )
                    }
                    DiscoveryScreenModel.Phase.Failed -> item {
                        EmptyState(
                            icon = YkIcons.Info,
                            title = stringResource(Res.string.discovery_failed_title),
                            body = stringResource(Res.string.discovery_failed_body),
                            actionLabel = stringResource(Res.string.discovery_retry),
                            onAction = model::retry,
                        )
                    }
                    DiscoveryScreenModel.Phase.Results -> {
                        val result = state.result ?: return@LazyColumn
                        item { Understood(result.query) }
                        if (result.results.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = YkIcons.Search,
                                    title = stringResource(Res.string.discovery_no_match_title),
                                    body = stringResource(Res.string.discovery_no_match_body),
                                    actionLabel = stringResource(Res.string.discovery_post_request),
                                    onAction = { navigator.push(NewRequestScreen()) },
                                )
                            }
                        } else {
                            item {
                                Text(
                                    stringResource(Res.string.discovery_results, result.results.size),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Yk.Ink3,
                                )
                            }
                            itemsIndexed(result.results, key = { _, r -> r.service.id }) { i, r ->
                                StaggeredItem(i) {
                                    ServiceListCard(
                                        service = r.service,
                                        distanceKm = r.reason.distanceKm,
                                        onClick = { navigator.push(ServiceDetailsScreen(r.service)) },
                                        footer = { ReasonBlock(r.reason, isDemo = r.service.isDemo) },
                                    )
                                }
                            }
                        }
                        val excluded = result.excluded.take(MAX_EXCLUDED_SHOWN)
                        if (excluded.isNotEmpty()) {
                            item {
                                Text(
                                    stringResource(Res.string.discovery_excluded_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Yk.Ink2,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                            itemsIndexed(excluded, key = { _, e -> "x-" + e.service.id }) { _, e -> ExcludedRow(e) }
                        }
                    }
                }
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }

    private companion object {
        const val MAX_EXCLUDED_SHOWN = 3
    }
}

@Composable
private fun SourceToggle(source: DiscoverySource, onChange: (DiscoverySource) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        YkChip(stringResource(Res.string.discovery_demo_toggle), source == DiscoverySource.Demo, { onChange(DiscoverySource.Demo) })
        YkChip(stringResource(Res.string.discovery_live_toggle), source == DiscoverySource.Live, { onChange(DiscoverySource.Live) })
    }
}

@Composable
private fun DemoBanner() {
    Row(
        Modifier.fillMaxWidth().clip(YkRadius.sm).background(Yk.WarningSoft).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(YkIcons.Info, null, tint = Yk.Warning, modifier = Modifier.size(18.dp))
        Text(stringResource(Res.string.discovery_demo_banner), style = MaterialTheme.typography.bodySmall, color = Yk.Ink2)
    }
}

@Composable
private fun LocationRow(state: DiscoveryScreenModel.State, onPick: () -> Unit) {
    val live = state.source == DiscoverySource.Live
    val label = when {
        !live -> stringResource(Res.string.discovery_demo_location)
        state.liveLocation != null -> stringResource(Res.string.discovery_location_label, state.liveLocation.label)
        else -> null
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) {
            Icon(YkIcons.MapPin, null, tint = Yk.Orange, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = Yk.OrangeDeep, modifier = Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(1f))
        }
        if (live) {
            YkChip(
                stringResource(if (label != null) Res.string.discovery_change_location else Res.string.discovery_pick_location),
                false,
                onPick,
                leading = YkIcons.MapPin.takeIf { label == null },
            )
        }
    }
}

@Composable
private fun Examples(onPick: (String) -> Unit) {
    val examples = listOf(
        "عندي مي عم تنزل من تحت المجلى وبدي حدا اليوم",
        "الكهربا قاطعة عن نص البيت والقاطع بيفصل",
        "the sofa has coffee stains, need cleaning today",
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.discovery_examples), style = MaterialTheme.typography.labelLarge, color = Yk.Ink3)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            examples.forEach { q -> YkChip(q, false, { onPick(q) }) }
        }
    }
}

/** What the engine understood: inferred trade(s) + requested time. */
@Composable
private fun Understood(query: ParsedQuery) {
    val parts = buildList {
        query.categoryWeights.entries.sortedByDescending { it.value }.filter { it.value >= 0.25 }.forEach {
            add(stringResource(categoryLabel(it.key)))
        }
        when (query.time) {
            TimeIntent.Now -> add(stringResource(Res.string.discovery_time_now))
            TimeIntent.Today -> add(stringResource(Res.string.discovery_time_today))
            TimeIntent.Tomorrow -> add(stringResource(Res.string.discovery_time_tomorrow))
            TimeIntent.None -> Unit
        }
    }
    if (parts.isEmpty()) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(YkIcons.Sparkles, null, tint = Yk.Orange, modifier = Modifier.size(16.dp))
        Text(
            stringResource(Res.string.discovery_understood, parts.joinToString(" · ")),
            style = MaterialTheme.typography.bodySmall,
            color = Yk.Ink2,
        )
    }
}

private fun categoryLabel(c: DiscoveryCategory): StringResource = when (c) {
    DiscoveryCategory.Plumbing -> Res.string.discovery_cat_plumbing
    DiscoveryCategory.Electrical -> Res.string.discovery_cat_electrical
    DiscoveryCategory.Cleaning -> Res.string.discovery_cat_cleaning
}

/** The grounded "why this matches" lines: quoted listing words, its schedule, its area. */
@Composable
private fun ReasonBlock(reason: MatchReason, isDemo: Boolean) {
    val language = LocalAppLanguage.current
    val quote: (String) -> String = { if (language == AppLanguage.English) "“$it”" else "«$it»" }
    val matchLine = when {
        reason.matchedTerms.isNotEmpty() ->
            stringResource(Res.string.discovery_reason_terms, reason.matchedTerms.joinToString("، ".takeIf { language == AppLanguage.Arabic } ?: ", ") { quote(it) })
        reason.categoryTag != null -> stringResource(Res.string.discovery_reason_category, reason.categoryTag)
        else -> null
    }
    val distance = distanceLabel(reason.distanceKm)
    val areaLine = if (reason.radiusIsDefault) {
        stringResource(Res.string.discovery_area_default, distance, reason.radiusKm.toInt())
    } else {
        stringResource(Res.string.discovery_area, distance)
    }
    val summary = listOfNotNull(matchLine, availabilityText(reason.availability), areaLine).joinToString(". ")
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(YkRadius.sm)
            .background(Yk.Fill)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) { contentDescription = summary },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (matchLine != null) ReasonLine(YkIcons.Sparkles, matchLine, Yk.Ink)
        ReasonLine(
            YkIcons.Clock,
            availabilityText(reason.availability),
            if (reason.availability.kind == AvailabilityNote.Kind.Unknown) Yk.Ink3 else Yk.Ink2,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.weight(1f)) { ReasonLine(YkIcons.MapPin, areaLine, Yk.Ink2) }
            if (isDemo) TagChip(stringResource(Res.string.discovery_demo_tag))
        }
    }
}

@Composable
private fun ReasonLine(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = Yk.Orange, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

/** Localized availability, built only from the listing's schedule fields. */
@Composable
fun availabilityText(note: AvailabilityNote): String {
    val until = hhmm(note.untilMinute)
    val range = hoursRange(note.fromMinute, note.untilMinute)
    return when (note.kind) {
        AvailabilityNote.Kind.Now ->
            if (note.aroundTheClock) stringResource(Res.string.discovery_avail_now_24h) else stringResource(Res.string.discovery_avail_now, until)
        AvailabilityNote.Kind.Today ->
            if (note.aroundTheClock) stringResource(Res.string.discovery_avail_today_24h) else stringResource(Res.string.discovery_avail_today, range)
        AvailabilityNote.Kind.Tomorrow ->
            if (note.aroundTheClock) stringResource(Res.string.discovery_avail_tomorrow_24h) else stringResource(Res.string.discovery_avail_tomorrow, range)
        AvailabilityNote.Kind.OpenToday ->
            if (note.aroundTheClock) stringResource(Res.string.discovery_avail_today_24h) else stringResource(Res.string.discovery_avail_open_today, range)
        AvailabilityNote.Kind.ClosedToday -> stringResource(Res.string.discovery_avail_closed_today)
        AvailabilityNote.Kind.Unknown -> stringResource(Res.string.discovery_avail_unknown)
    }
}

/** "08:00–22:00" wrapped in a left-to-right isolate so RTL text keeps the order. */
fun hoursRange(fromMinute: Int, untilMinute: Int): String = "⁦${hhmm(fromMinute)}–${hhmm(untilMinute)}⁩"

/** "08:00" from minutes after midnight (24h clock, no locale guessing). */
fun hhmm(minutes: Int): String {
    val m = minutes.coerceIn(0, 24 * 60)
    val h = m / 60
    val mm = m % 60
    return "${if (h < 10) "0" else ""}$h:${if (mm < 10) "0" else ""}$mm"
}

@Composable
private fun ExcludedRow(e: ExcludedListing) {
    val why = when (e.reason) {
        ExclusionReason.OutsideArea -> stringResource(
            Res.string.discovery_excluded_outside,
            distanceLabel(e.distanceKm ?: 0.0),
            (e.radiusKm ?: 0.0).toInt(),
        )
        ExclusionReason.Unavailable -> stringResource(Res.string.discovery_excluded_unavailable)
        ExclusionReason.UnknownAvailability -> stringResource(Res.string.discovery_excluded_unknown_avail)
        ExclusionReason.UnknownLocation -> stringResource(Res.string.discovery_excluded_unknown_location)
    }
    Row(
        Modifier.fillMaxWidth().clip(YkRadius.sm).background(Yk.Fill).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(YkIcons.Info, null, tint = Yk.Ink3, modifier = Modifier.size(16.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(e.service.title, style = MaterialTheme.typography.titleSmall, color = Yk.Ink2)
            Text(why, style = MaterialTheme.typography.bodySmall, color = Yk.Ink3)
        }
        if (e.service.isDemo) TagChip(stringResource(Res.string.discovery_demo_tag))
    }
}
