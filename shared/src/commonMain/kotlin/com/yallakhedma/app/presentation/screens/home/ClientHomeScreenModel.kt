package com.yallakhedma.app.presentation.screens.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.model.ServiceCategory
import com.yallakhedma.app.domain.repository.CategoryRepository
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
    categoryRepo: CategoryRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            providerRepo.observeFeatured()
                .catch { _state.update { it.copy(providersLoading = false) } }
                .collect { list -> _state.update { it.copy(providers = list, providersLoading = false) } }
        }
        screenModelScope.launch {
            serviceRepo.observeNearby()
                .catch { _state.update { it.copy(servicesLoading = false) } }
                .collect { list -> _state.update { it.copy(services = list, servicesLoading = false) } }
        }
        screenModelScope.launch {
            categoryRepo.observeAll()
                .catch { _state.update { it.copy(categoriesLoading = false) } }
                .collect { list ->
                    _state.update { it.copy(categories = list.sortedBy { c -> c.order }, categoriesLoading = false) }
                }
        }
    }

    data class HomeState(
        val providers: List<Provider> = emptyList(),
        val providersLoading: Boolean = true,
        val services: List<Service> = emptyList(),
        val servicesLoading: Boolean = true,
        val categories: List<ServiceCategory> = emptyList(),
        val categoriesLoading: Boolean = true,
    )
}
