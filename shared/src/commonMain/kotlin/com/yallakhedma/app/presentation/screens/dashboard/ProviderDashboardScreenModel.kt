package com.yallakhedma.app.presentation.screens.dashboard

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.ProviderRepository
import com.yallakhedma.app.domain.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProviderDashboardScreenModel(
    private val authRepository: AuthRepository,
    private val providerRepository: ProviderRepository,
    private val requestRepository: RequestRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        val uid = authRepository.currentUid()
        if (uid != null) {
            screenModelScope.launch {
                providerRepository.observeById(uid)
                    .catch { /* keep defaults */ }
                    .collect { p -> _state.update { it.copy(provider = p, loading = false) } }
            }
            screenModelScope.launch {
                requestRepository.observePendingForProvider(uid)
                    .catch { /* ignore */ }
                    .collect { list -> _state.update { it.copy(requests = list) } }
            }
        } else {
            _state.update { it.copy(loading = false) }
        }
    }

    fun accept(requestId: String) = updateStatus(requestId, ServiceRequest.STATUS_ACCEPTED)
    fun reject(requestId: String) = updateStatus(requestId, ServiceRequest.STATUS_REJECTED)

    private fun updateStatus(requestId: String, status: String) {
        screenModelScope.launch { requestRepository.updateStatus(requestId, status) }
    }

    fun signOut() {
        screenModelScope.launch { authRepository.signOut() }
    }

    data class State(
        val provider: Provider? = null,
        val requests: List<ServiceRequest> = emptyList(),
        val loading: Boolean = true,
    )
}
