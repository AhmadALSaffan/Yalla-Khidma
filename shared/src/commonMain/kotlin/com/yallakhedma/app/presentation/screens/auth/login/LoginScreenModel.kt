package com.yallakhedma.app.presentation.screens.auth.login

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.auth.SocialAuthClient
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginScreenModel(
    private val authRepository: AuthRepository,
    private val socialAuthClient: SocialAuthClient,
) : ScreenModel {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }
    fun togglePasswordVisibility() = _state.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun submit() {
        if (!validate()) return
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val current = state.value
            handleResult(authRepository.signInWithEmail(current.email.trim(), current.password))
        }
    }

    fun signInWithGoogle() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val idToken = socialAuthClient.signInWithGoogle()
            if (idToken == null) {
                _state.update { it.copy(isLoading = false, error = "تعذّر الدخول بحساب Google") }
                return@launch
            }
            handleResult(authRepository.signInWithGoogleCredential(idToken))
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
            is DataResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            DataResult.Loading -> Unit
        }
    }

    private fun validate(): Boolean {
        val s = state.value
        return when {
            s.email.isBlank() -> { _state.update { it.copy(error = "الإيميل مطلوب") }; false }
            !s.email.contains("@") -> { _state.update { it.copy(error = "إيميل غير صحيح") }; false }
            s.password.length < 6 -> { _state.update { it.copy(error = "كلمة المرور 6 أحرف على الأقل") }; false }
            else -> true
        }
    }
}
