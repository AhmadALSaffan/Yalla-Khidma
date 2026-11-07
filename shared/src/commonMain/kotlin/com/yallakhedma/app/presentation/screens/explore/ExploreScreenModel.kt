package com.yallakhedma.app.presentation.screens.explore

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.model.ServiceCategory
import com.yallakhedma.app.domain.repository.CategoryRepository
import com.yallakhedma.app.domain.repository.ProviderRepository
import com.yallakhedma.app.domain.repository.ServiceRepository
import com.yallakhedma.app.util.ClientLocationHolder
import com.yallakhedma.app.util.PickedLocation
import com.yallakhedma.app.util.haversineKm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ExploreMode { Services, Providers }

/** A service with its distance from the client's picked location (km), if known. */
data class ServiceHit(val service: Service, val distanceKm: Double?)

/** Search + browse over services and providers, filterable by category. */
class ExploreScreenModel(
    serviceRepository: ServiceRepository,
    providerRepository: ProviderRepository,
    categoryRepository: CategoryRepository,
    private val locationHolder: ClientLocationHolder,
) : ScreenModel {

    data class State(
        val mode: ExploreMode = ExploreMode.Services,
        val query: String = "",
        val categoryId: String? = null,
        val categories: List<ServiceCategory> = emptyList(),
        val allServices: List<Service> = emptyList(),
        val allProviders: List<Provider> = emptyList(),
        val servicesLoading: Boolean = true,
        val providersLoading: Boolean = true,
        val locationLabel: String? = null,
        val failed: Boolean = false,
    ) {
        private val category get() = categories.firstOrNull { it.id == categoryId }

        val services: List<ServiceHit>
            get() = allServices
                .filter { matchesCategory(it.categoryTag) && matchesQuery(it.title, it.description, it.providerName, it.categoryTag) }
                .map { ServiceHit(it, null) }

        val providers: List<Provider>
            get() = allProviders
                .filter { matchesCategory(it.category, it.profession) && matchesQuery(it.name, it.profession, it.bio, it.city) }
                .sortedByDescending { it.rating }

        val loading: Boolean get() = if (mode == ExploreMode.Services) servicesLoading else providersLoading

        private fun matchesCategory(vararg fields: String): Boolean {
            val c = category ?: return true
            val names = listOf(c.nameAr, c.nameEn, c.id).filter { it.isNotBlank() }.map { it.lowercase() }
            return fields.any { f -> val v = f.lowercase(); v.isNotBlank() && names.any { n -> v.contains(n) || n.contains(v) } }
        }

        private fun matchesQuery(vararg fields: String): Boolean {
            val q = query.trim().lowercase()
            if (q.isEmpty()) return true
            return fields.any { it.lowercase().contains(q) }
        }
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            serviceRepository.observeAll()
                .catch { _state.update { it.copy(servicesLoading = false, failed = true) } }
                .collect { list -> _state.update { it.copy(allServices = list, servicesLoading = false, failed = false) } }
        }
        screenModelScope.launch {
            providerRepository.observeAll()
                .catch { _state.update { it.copy(providersLoading = false, failed = true) } }
                .collect { list -> _state.update { it.copy(allProviders = list, providersLoading = false) } }
        }
        screenModelScope.launch {
            categoryRepository.observeAll()
                .catch { }
                .collect { list -> _state.update { it.copy(categories = list.sortedBy { c -> c.order }) } }
        }
        screenModelScope.launch {
            locationHolder.location.collect { loc -> _state.update { it.copy(locationLabel = loc?.label) } }
        }
    }

    private var initialized = false

    fun init(mode: ExploreMode, categoryId: String?) {
        if (initialized) return
        initialized = true
        _state.update { it.copy(mode = mode, categoryId = categoryId) }
    }

    fun setMode(mode: ExploreMode) = _state.update { it.copy(mode = mode) }
    fun setQuery(q: String) = _state.update { it.copy(query = q) }
    fun setCategory(id: String?) = _state.update { it.copy(categoryId = id) }

    /** Services sorted nearest-first when the client has picked a location. */
    fun sortedServices(state: State): List<ServiceHit> =
        state.services.map { it.service }.withDistances(locationHolder.location.value)
}

/** Annotates with distance from [origin] and sorts nearest-first (unknown coords last). */
fun List<Service>.withDistances(origin: PickedLocation?): List<ServiceHit> {
    if (origin == null) return map { ServiceHit(it, null) }
    return map { s ->
        val d = if (s.latitude != 0.0 || s.longitude != 0.0) {
            haversineKm(origin.latitude, origin.longitude, s.latitude, s.longitude)
        } else null
        ServiceHit(s, d)
    }.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
}
