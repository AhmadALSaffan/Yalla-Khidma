package com.yallakhedma.app.presentation.screens.auth.login

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.presentation.i18n.UiText
import com.yallakhedma.app.presentation.i18n.toUiText
import com.yallakhedma.app.util.AuthValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.error_email_invalid
import yallakhidma.shared.generated.resources.error_email_required
import yallakhidma.shared.generated.resources.error_password_required

/** Email + password sign-in (the secondary path — Google lives on the Welcome screen). */
class LoginScreenModel(
    private val authRepository: AuthRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, emailError = null, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, passwordError = null, error = null) }
    fun togglePasswordVisibility() = _state.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun submit() {
        if (state.value.isLoading || !validate()) return
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val current = state.value
            handleResult(authRepository.signInWithEmail(current.email.trim(), current.password))
        }
    }

    /**
     * Apple sign-in is driven from Swift on iOS. Swift obtains (idToken, rawNonce) and calls
     * this method via the exported `AuthRepository`. Here we expose a hook for completeness.
     */
    fun signInWithApple(idToken: String, rawNonce: String) {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            handleResult(authRepository.signInWithAppleCredential(idToken, rawNonce))
        }
    }

    private fun handleResult(result: DataResult<*>) {
        when (result) {
            is DataResult.Success -> _state.update { it.copy(isLoading = false, signedIn = true) }
            is DataResult.Error -> _state.update { it.copy(isLoading = false, error = result.toUiText()) }
            DataResult.Loading -> Unit
        }
    }

    private fun validate(): Boolean {
        val s = state.value
        val emailError = when {
            s.email.isBlank() -> UiText.of(Res.string.error_email_required)
            !AuthValidation.isValidEmail(s.email) -> UiText.of(Res.string.error_email_invalid)
            else -> null
        }
        // On login we only check the field is filled — never enforce a
        // length/complexity policy here (it leaks the rule and can lock out
        // accounts created under an older policy). Firebase rejects bad creds.
        val passwordError = if (s.password.isBlank()) UiText.of(Res.string.error_password_required) else null
        _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
        return emailError == null && passwordError == null
    }
}
