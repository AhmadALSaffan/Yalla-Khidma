package com.yallakhedma.app.presentation.screens.discovery

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.demo.DemoDiscoveryCatalog
import com.yallakhedma.app.domain.discovery.DiscoveryEngine
import com.yallakhedma.app.domain.discovery.DiscoveryResult
import com.yallakhedma.app.domain.discovery.GeoPoint
import com.yallakhedma.app.domain.discovery.SearchContext
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.repository.ServiceRepository
import com.yallakhedma.app.util.ClientLocationHolder
import com.yallakhedma.app.util.PickedLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Where listings come from: the labeled demo fixtures, or live Firestore services. */
enum class DiscoverySource { Demo, Live }

/** Natural-language discovery: parse → retrieve → hard filters → ranked, explained results. */
class DiscoveryScreenModel(
    private val serviceRepository: ServiceRepository,
    private val locationHolder: ClientLocationHolder,
) : ScreenModel {

    enum class Phase { Idle, Searching, Results, NeedsLocation, Failed }

    data class State(
        val source: DiscoverySource = DiscoverySource.Demo,
        val query: String = "",
        val phase: Phase = Phase.Idle,
        val result: DiscoveryResult? = null,
        val liveLocation: PickedLocation? = null,
        val locationFailed: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val engine = DiscoveryEngine()

    // Live listings: null until the first snapshot; failure kept separately so retry can reset it.
    private val liveListings = MutableStateFlow<List<Service>?>(null)
    private val liveFailed = MutableStateFlow(false)
    private var liveJob: Job? = null
    private var searchJob: Job? = null

    init {
        screenModelScope.launch {
            locationHolder.location.collect { loc -> _state.update { it.copy(liveLocation = loc) } }
        }
    }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun setSource(source: DiscoverySource) {
        if (source == _state.value.source) return
        _state.update { it.copy(source = source, result = null, phase = Phase.Idle) }
        if (_state.value.query.isNotBlank()) search()
    }

    fun onLocationPicked(loc: PickedLocation?) {
        if (loc == null) {
            _state.update { it.copy(locationFailed = true) }
            return
        }
        locationHolder.set(loc)
        _state.update { it.copy(liveLocation = loc, locationFailed = false) }
        if (_state.value.phase == Phase.NeedsLocation) search()
    }

    fun retry() {
        liveJob?.cancel()
        liveJob = null
        liveListings.value = null
        liveFailed.value = false
        search()
    }

    fun search() {
        val s = _state.value
        val query = s.query.trim()
        if (query.isEmpty()) return
        searchJob?.cancel()
        searchJob = screenModelScope.launch {
            _state.update { it.copy(phase = Phase.Searching) }
            val now = Clock.System.now()
            val (listings, location) = when (s.source) {
                DiscoverySource.Demo -> {
                    val today = now.toLocalDateTime(TimeZone.of(DemoDiscoveryCatalog.TIME_ZONE)).date
                    DemoDiscoveryCatalog.services(today) to DemoDiscoveryCatalog.clientLocation
                }
                DiscoverySource.Live -> {
                    val loc = s.liveLocation ?: locationHolder.location.value
                    if (loc == null) {
                        _state.update { it.copy(phase = Phase.NeedsLocation, result = null) }
                        return@launch
                    }
                    val list = loadLive()
                    if (list == null) {
                        _state.update { it.copy(phase = Phase.Failed, result = null) }
                        return@launch
                    }
                    list to GeoPoint(loc.latitude, loc.longitude)
                }
            }
            val result = runCatching {
                withContext(Dispatchers.Default) {
                    engine.search(query, listings, SearchContext(clientLocation = location, now = now))
                }
            }.getOrNull()
            _state.update {
                if (result == null) it.copy(phase = Phase.Failed, result = null)
                else it.copy(phase = Phase.Results, result = result)
            }
        }
    }

    /** First snapshot of live services, or null if Firestore failed. */
    private suspend fun loadLive(): List<Service>? {
        if (liveJob == null) {
            liveJob = screenModelScope.launch {
                serviceRepository.observeAll(limit = LIVE_LIMIT)
                    .catch { liveFailed.value = true }
                    .collect { liveListings.value = it }
            }
        }
        // Wait for whichever comes first: data or failure.
        return kotlinx.coroutines.flow.combine(liveListings, liveFailed) { list, failed -> list to failed }
            .first { (list, failed) -> list != null || failed }
            .let { (list, failed) -> if (failed) null else list }
    }

    private companion object {
        const val LIVE_LIMIT = 300
    }
}
