package com.yallakhedma.app.presentation.screens.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.User
import com.yallakhedma.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientProfileScreenModel(
    private val authRepository: AuthRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            authRepository.currentUser.collect { u ->
                _state.update { it.copy(user = u) }
            }
        }
    }

    fun signOut() {
        screenModelScope.launch { authRepository.signOut() }
    }

    data class State(val user: User? = null)
}
