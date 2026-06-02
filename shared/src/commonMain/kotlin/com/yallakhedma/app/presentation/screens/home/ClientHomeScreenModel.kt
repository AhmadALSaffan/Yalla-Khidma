package com.yallakhedma.app.presentation.screens.home

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

class ClientHomeScreenModel(
    providerRepo: ProviderRepository,
    serviceRepo: ServiceRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            providerRepo.observeFeatured()
                .catch { e ->
                    _state.update { it.copy(providersError = e.message, providersLoading = false) }
                }
                .collect { list ->
                    _state.update {
                        it.copy(
                            providers = list,
                            providersLoading = false,
                            providersError = null,
                        )
                    }
                }
        }
        screenModelScope.launch {
            serviceRepo.observeNearby()
                .catch { e ->
                    _state.update { it.copy(servicesError = e.message, servicesLoading = false) }
                }
                .collect { list ->
                    _state.update {
                        it.copy(
                            services = list,
                            servicesLoading = false,
                            servicesError = null,
                        )
                    }
                }
        }
    }

    data class HomeState(
        val providers: List<Provider> = emptyList(),
        val providersLoading: Boolean = true,
        val providersError: String? = null,
        val services: List<Service> = emptyList(),
        val servicesLoading: Boolean = true,
        val servicesError: String? = null,
    )
}
