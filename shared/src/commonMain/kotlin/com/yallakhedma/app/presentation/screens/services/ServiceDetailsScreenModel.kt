package com.yallakhedma.app.presentation.screens.services

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.ProviderRepository
import com.yallakhedma.app.domain.repository.RequestRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ServiceDetailsScreenModel(
    private val providerRepository: ProviderRepository,
    private val requestRepository: RequestRepository,
    private val authRepository: AuthRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var loaded = false

    /** Loads the linked provider so the page can show their photo + details. */
    fun loadProvider(providerId: String) {
        if (loaded || providerId.isBlank()) return
        loaded = true
        screenModelScope.launch {
            providerRepository.observeById(providerId)
                .catch { /* ignore — provider card just won't show */ }
                .collect { p -> _state.update { it.copy(provider = p) } }
        }
    }

    /** Books the service: creates a request for the provider + bumps the counter. */
    fun book(service: Service) {
        if (state.value.booked || state.value.booking) return
        screenModelScope.launch {
            _state.update { it.copy(booking = true, error = null) }
            val client = authRepository.currentUser.firstOrNull()
            val clientId = client?.id.orEmpty()
            val clientName = client?.name.orEmpty()
            val clientPhotoUrl = client?.photoUrl.orEmpty()

            // Create the request the provider will see (only if linked + we know who booked).
            if (service.providerId.isNotBlank() && clientId.isNotBlank()) {
                val provider = state.value.provider
                val request = ServiceRequest(
                    serviceId = service.id,
                    serviceTitle = service.title,
                    providerId = service.providerId,
                    clientId = clientId,
                    clientName = clientName,
                    clientPhotoUrl = clientPhotoUrl,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                    serviceImageUrl = service.imageUrl,
                    serviceDescription = service.description,
                    servicePrice = service.priceFrom,
                    serviceCategoryTag = service.categoryTag,
                    providerName = provider?.name ?: service.providerName,
                    providerPhotoUrl = provider?.photoUrl.orEmpty(),
                    providerProfession = provider?.profession.orEmpty(),
                )
                val created = requestRepository.create(request)
                if (created is DataResult.Error) {
                    _state.update { it.copy(booking = false, error = created.message) }
                    return@launch
                }
                providerRepository.incrementBookings(service.providerId)
            }
            _state.update { it.copy(booking = false, booked = true) }
        }
    }

    data class State(
        val provider: Provider? = null,
        val booking: Boolean = false,
        val booked: Boolean = false,
        val error: String? = null,
    )
}
