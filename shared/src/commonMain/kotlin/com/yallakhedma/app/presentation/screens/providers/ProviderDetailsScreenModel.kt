package com.yallakhedma.app.presentation.screens.providers

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

class ProviderDetailsScreenModel(
    private val providerRepository: ProviderRepository,
    private val serviceRepository: ServiceRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var loaded = false

    /** Streams the live provider (for up-to-date bookings count) + their services. */
    fun load(providerId: String) {
        if (loaded || providerId.isBlank()) return
        loaded = true
        screenModelScope.launch {
            providerRepository.observeById(providerId)
                .catch { /* keep the passed-in provider */ }
                .collect { p -> if (p != null) _state.update { it.copy(provider = p) } }
        }
        screenModelScope.launch {
            serviceRepository.observeByProvider(providerId)
                .catch { _state.update { it.copy(servicesLoading = false) } }
                .collect { list ->
                    _state.update { it.copy(services = list, servicesLoading = false) }
                }
        }
    }

    data class State(
        val provider: Provider? = null,
        val services: List<Service> = emptyList(),
        val servicesLoading: Boolean = true,
    )
}
