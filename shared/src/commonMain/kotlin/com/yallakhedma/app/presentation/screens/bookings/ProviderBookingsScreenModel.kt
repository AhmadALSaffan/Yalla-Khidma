package com.yallakhedma.app.presentation.screens.bookings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("OPT_IN_USAGE")
class ProviderBookingsScreenModel(
    private val authRepository: AuthRepository,
    private val requestRepository: RequestRepository,
) : ScreenModel {

    enum class Filter { All, Pending, Accepted, Paid, Rejected }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            authRepository.currentUser
                .flatMapLatest { user ->
                    if (user == null) flowOf(emptyList())
                    else requestRepository.observeAllForProvider(user.id)
                }
                .collectLatest { all ->
                    _state.update { it.copy(all = all, loading = false) }
                }
        }
    }

    fun setFilter(filter: Filter) = _state.update { it.copy(filter = filter) }

    fun accept(id: String) = screenModelScope.launch {
        requestRepository.updateStatus(id, ServiceRequest.STATUS_ACCEPTED)
    }

    fun reject(id: String) = screenModelScope.launch {
        requestRepository.updateStatus(id, ServiceRequest.STATUS_REJECTED)
    }

    data class State(
        val all: List<ServiceRequest> = emptyList(),
        val filter: Filter = Filter.All,
        val loading: Boolean = true,
    ) {
        val filtered: List<ServiceRequest> = when (filter) {
            Filter.All -> all
            Filter.Pending -> all.filter { it.status == ServiceRequest.STATUS_PENDING }
            Filter.Accepted -> all.filter { it.status == ServiceRequest.STATUS_ACCEPTED && !it.paid }
            Filter.Paid -> all.filter { it.paid }
            Filter.Rejected -> all.filter { it.status == ServiceRequest.STATUS_REJECTED }
        }

        val counts: Map<Filter, Int> = mapOf(
            Filter.All to all.size,
            Filter.Pending to all.count { it.status == ServiceRequest.STATUS_PENDING },
            Filter.Accepted to all.count { it.status == ServiceRequest.STATUS_ACCEPTED && !it.paid },
            Filter.Paid to all.count { it.paid },
            Filter.Rejected to all.count { it.status == ServiceRequest.STATUS_REJECTED },
        )
    }
}
