package com.yallakhedma.app.presentation.screens.bookings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.RequestRepository
import com.yallakhedma.app.domain.repository.ServiceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("OPT_IN_USAGE")
class MyBookingsScreenModel(
    private val authRepository: AuthRepository,
    private val requestRepository: RequestRepository,
    private val serviceRepository: ServiceRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    // Tracks per-service image-fetch jobs so we don't subscribe twice for the
    // same service when the bookings list updates.
    private val imageJobs = mutableMapOf<String, Job>()

    init {
        screenModelScope.launch {
            authRepository.currentUser
                .flatMapLatest { user ->
                    if (user == null) flowOf(emptyList())
                    else requestRepository.observeForClient(user.id)
                }
                .collectLatest { bookings ->
                    _state.update { it.copy(bookings = bookings, loading = false) }
                    // Kick off lookups for any booking missing a snapshotted image.
                    bookings
                        .filter { it.serviceImageUrl.isBlank() && it.serviceId.isNotBlank() }
                        .map { it.serviceId }
                        .distinct()
                        .forEach(::ensureImageFor)
                }
        }
    }

    private fun ensureImageFor(serviceId: String) {
        if (imageJobs.containsKey(serviceId)) return
        imageJobs[serviceId] = screenModelScope.launch {
            serviceRepository.observeById(serviceId)
                .catch { /* network/perm error — leave the orange fallback */ }
                .collect { service ->
                    val img = service?.imageUrl.orEmpty()
                    if (img.isNotBlank()) {
                        _state.update { it.copy(liveImages = it.liveImages + (serviceId to img)) }
                    }
                }
        }
    }

    data class State(
        val bookings: List<ServiceRequest> = emptyList(),
        val loading: Boolean = true,
        val liveImages: Map<String, String> = emptyMap(),
    )
}
