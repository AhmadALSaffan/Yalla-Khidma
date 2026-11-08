package com.yallakhedma.app.presentation.screens.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.auth.SocialAuthClient
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.SignInMethod
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.domain.util.ErrorCode
import com.yallakhedma.app.presentation.i18n.UiText
import com.yallakhedma.app.presentation.i18n.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.error_password_required
import yallakhidma.shared.generated.resources.error_wrong_password

/**
 * "Confirm it's you" before editing the provider profile. Replaces the old
 * emailed OTP (which needed Cloud Functions / the Blaze plan) with Firebase
 * re-authentication: password for email accounts, Google for Google accounts.
 */
class ConfirmIdentityScreenModel(
    private val authRepository: AuthRepository,
    private val socialAuthClient: SocialAuthClient,
) : ScreenModel {

    data class State(
        val method: SignInMethod = SignInMethod.Other,
        val password: String = "",
        val passwordVisible: Boolean = false,
        val loading: Boolean = false,
        val error: UiText? = null,
        val confirmed: Boolean = false,
    )

    private val _state = MutableStateFlow(State(method = authRepository.signInMethod()))
    val state = _state.asStateFlow()

    fun onPassword(v: String) = _state.update { it.copy(password = v, error = null) }
    fun toggleVisible() = _state.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun confirmWithPassword() {
        val pw = state.value.password
        if (pw.isBlank()) {
            _state.update { it.copy(error = UiText.of(Res.string.error_password_required)) }
            return
        }
        attempt { authRepository.reauthenticateWithPassword(pw) }
    }

    fun confirmWithGoogle() = attempt {
        val token = socialAuthClient.signInWithGoogle()
            ?: return@attempt DataResult.Error("cancelled", code = ErrorCode.SocialCancelled)
        authRepository.reauthenticateWithGoogle(token)
    }

    private fun attempt(block: suspend () -> DataResult<Unit>) {
        if (state.value.loading) return
        screenModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = block()) {
                is DataResult.Success -> _state.update { it.copy(loading = false, confirmed = true) }
                is DataResult.Error -> _state.update {
                    it.copy(
                        loading = false,
                        error = if (r.code == ErrorCode.InvalidCredentials) UiText.of(Res.string.error_wrong_password) else r.toUiText(),
                    )
                }
                DataResult.Loading -> Unit
            }
        }
    }
}
