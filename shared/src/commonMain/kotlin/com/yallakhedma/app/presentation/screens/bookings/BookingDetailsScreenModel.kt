package com.yallakhedma.app.presentation.screens.bookings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.repository.ProviderRepository
import com.yallakhedma.app.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads the live Service and Provider for a booking so the detail page can
 * display fields (especially the picture the provider added to the service)
 * even when the booking was created before we started snapshotting them.
 */
class BookingDetailsScreenModel(
    private val serviceRepository: ServiceRepository,
    private val providerRepository: ProviderRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var loaded = false

    fun load(serviceId: String, providerId: String) {
        if (loaded) return
        loaded = true

        if (serviceId.isNotBlank()) {
            screenModelScope.launch {
                serviceRepository.observeById(serviceId)
                    .catch { /* ignore — fall back to snapshot data on the booking */ }
                    .collect { s -> _state.update { it.copy(service = s) } }
            }
        }
        if (providerId.isNotBlank()) {
            screenModelScope.launch {
                providerRepository.observeById(providerId)
                    .catch { /* ignore */ }
                    .collect { p -> _state.update { it.copy(provider = p) } }
            }
        }
    }

    data class State(
        val service: Service? = null,
        val provider: Provider? = null,
    )
}
