package com.yallakhedma.app.presentation.screens.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.User
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProviderProfileScreenModel(
    private val authRepository: AuthRepository,
    private val providerRepository: ProviderRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            authRepository.currentUser.collect { u -> _state.update { it.copy(user = u) } }
        }
        val uid = authRepository.currentUid()
        if (uid != null) {
            screenModelScope.launch {
                providerRepository.observeById(uid)
                    .catch { /* ignore */ }
                    .collect { p -> _state.update { it.copy(provider = p) } }
            }
        }
    }

    fun signOut() { screenModelScope.launch { authRepository.signOut() } }

    data class State(val user: User? = null, val provider: Provider? = null)
}
