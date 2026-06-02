package com.yallakhedma.app.presentation.screens.services

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AllServicesScreenModel(
    repository: ServiceRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            repository.observeAll()
                .catch { e -> _state.update { it.copy(loading = false, error = e.message) } }
                .collect { list ->
                    _state.update { it.copy(services = list, loading = false, error = null) }
                }
        }
    }

    data class State(
        val services: List<Service> = emptyList(),
        val loading: Boolean = true,
        val error: String? = null,
    )
}
