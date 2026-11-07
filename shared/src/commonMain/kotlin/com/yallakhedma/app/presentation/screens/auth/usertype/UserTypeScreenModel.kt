package com.yallakhedma.app.presentation.screens.auth.usertype

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.User
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.presentation.i18n.UiText
import com.yallakhedma.app.presentation.i18n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserTypeState(
    val selected: UserType? = null,
    val signedInEmail: String? = null,
    val isSaving: Boolean = false,
    val error: UiText? = null,
    /** Set after the type is saved (complete-account mode) — the screen routes on it. */
    val savedUser: User? = null,
    val signedOut: Boolean = false,
)

class UserTypeScreenModel(
    private val authRepository: AuthRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(UserTypeState())
    val state = _state.asStateFlow()

    fun loadSignedInUser() {
        screenModelScope.launch {
            val email = authRepository.currentUser.firstOrNull()?.email
            _state.update { it.copy(signedInEmail = email) }
        }
    }

    fun select(type: UserType) = _state.update { it.copy(selected = type, error = null) }

    /** Complete-account mode: persist the choice for the signed-in (Google) user. */
    fun save() {
        val type = state.value.selected ?: return
        if (state.value.isSaving) return
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            when (val result = authRepository.chooseUserType(type)) {
                is DataResult.Success -> _state.update { it.copy(isSaving = false, savedUser = result.data) }
                is DataResult.Error -> _state.update { it.copy(isSaving = false, error = result.toUiText()) }
                DataResult.Loading -> Unit
            }
        }
    }

    fun signOut() {
        screenModelScope.launch {
            authRepository.signOut()
            _state.update { it.copy(signedOut = true) }
        }
    }
}
