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
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class ProviderDashboardScreenModel(
    private val authRepository: AuthRepository,
    private val providerRepository: ProviderRepository,
    private val requestRepository: RequestRepository,
) : ScreenModel {

    /** One bar of the weekly chart. */
    data class DayCount(val date: LocalDate, val count: Int)

    data class State(
        val provider: Provider? = null,
        val all: List<ServiceRequest> = emptyList(),
        val loading: Boolean = true,
    ) {
        val pending: List<ServiceRequest> get() = all.filter { it.status == ServiceRequest.STATUS_PENDING }

        /** Sum actually charged on bookings paid this calendar month. */
        val monthEarnings: Double
            get() {
                val tz = TimeZone.currentSystemDefault()
                val now = Clock.System.now().toLocalDateTime(tz)
                return all.filter { it.paid }.filter {
                    val d = Instant.fromEpochMilliseconds(it.paidAt.takeIf { t -> t > 0 } ?: it.createdAt).toLocalDateTime(tz)
                    d.year == now.year && d.monthNumber == now.monthNumber
                }.sumOf { if (it.paidAmount > 0) it.paidAmount else it.proposedPrice }
            }

        /** Requests received on each of the last 7 days (oldest first). */
        val lastSevenDays: List<DayCount>
            get() {
                val tz = TimeZone.currentSystemDefault()
                val today = Clock.System.now().toLocalDateTime(tz).date
                val byDay = all.groupingBy { Instant.fromEpochMilliseconds(it.createdAt).toLocalDateTime(tz).date }.eachCount()
                return (6 downTo 0).map { back ->
                    val day = today.minus(DatePeriod(days = back))
                    DayCount(day, byDay[day] ?: 0)
                }
            }
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        val uid = authRepository.currentUid()
        if (uid != null) {
            screenModelScope.launch {
                providerRepository.observeById(uid)
                    .catch { _state.update { it.copy(loading = false) } }
                    .collect { p -> _state.update { it.copy(provider = p, loading = false) } }
            }
            screenModelScope.launch {
                requestRepository.observeAllForProvider(uid)
                    .catch { }
                    .collect { list -> _state.update { it.copy(all = list) } }
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
}
