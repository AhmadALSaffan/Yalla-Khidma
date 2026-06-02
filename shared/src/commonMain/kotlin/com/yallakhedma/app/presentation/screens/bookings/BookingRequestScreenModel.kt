package com.yallakhedma.app.presentation.screens.bookings

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

class BookingRequestScreenModel(
    private val authRepository: AuthRepository,
    private val providerRepository: ProviderRepository,
    private val requestRepository: RequestRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var providerLoaded = false

    fun loadProvider(providerId: String) {
        if (providerLoaded || providerId.isBlank()) return
        providerLoaded = true
        screenModelScope.launch {
            providerRepository.observeById(providerId)
                .catch { /* ignore */ }
                .collect { p -> _state.update { it.copy(provider = p) } }
        }
    }

    fun onPrice(raw: String) {
        // Allow only digits + at most one decimal separator.
        val cleaned = buildString {
            var dotSeen = false
            for (c in raw) {
                if (c.isDigit()) append(c)
                else if ((c == '.' || c == ',') && !dotSeen) {
                    append('.')
                    dotSeen = true
                }
            }
        }.take(10)
        _state.update { it.copy(price = cleaned, error = null) }
    }

    fun submit(service: Service) {
        val s = state.value
        if (s.submitting || s.submitted) return
        val price = s.price.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            _state.update { it.copy(error = "أدخل سعراً صحيحاً") }
            return
        }
        screenModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            val client = authRepository.currentUser.firstOrNull()
            val clientId = client?.id.orEmpty()
            if (service.providerId.isBlank() || clientId.isBlank()) {
                _state.update { it.copy(submitting = false, error = "تعذّر إرسال الطلب") }
                return@launch
            }
            val provider = state.value.provider
            val request = ServiceRequest(
                serviceId = service.id,
                serviceTitle = service.title,
                providerId = service.providerId,
                clientId = clientId,
                clientName = client?.name.orEmpty(),
                clientPhotoUrl = client?.photoUrl.orEmpty(),
                createdAt = Clock.System.now().toEpochMilliseconds(),
                serviceImageUrl = service.imageUrl,
                serviceDescription = service.description,
                servicePrice = service.priceFrom,
                serviceCategoryTag = service.categoryTag,
                providerName = provider?.name ?: service.providerName,
                providerPhotoUrl = provider?.photoUrl.orEmpty(),
                providerProfession = provider?.profession.orEmpty(),
                proposedPrice = price,
            )
            when (val r = requestRepository.create(request)) {
                is DataResult.Success -> {
                    providerRepository.incrementBookings(service.providerId)
                    _state.update { it.copy(submitting = false, submitted = true) }
                }
                is DataResult.Error -> _state.update { it.copy(submitting = false, error = r.message) }
                DataResult.Loading -> Unit
            }
        }
    }

    data class State(
        val provider: Provider? = null,
        val price: String = "",
        val submitting: Boolean = false,
        val submitted: Boolean = false,
        val error: String? = null,
    )
}
