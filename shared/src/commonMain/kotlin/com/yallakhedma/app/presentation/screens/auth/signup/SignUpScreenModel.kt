package com.yallakhedma.app.presentation.screens.auth.signup

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpScreenModel(
    private val authRepository: AuthRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }
    fun onConfirmPasswordChange(value: String) = _state.update { it.copy(confirmPassword = value, error = null) }
    fun togglePasswordVisibility() = _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun onUserTypeChange(type: UserType) = _state.update { it.copy(userType = type) }

    /** Sets the type chosen on the preceding selection screen (once). */
    fun setInitialUserType(type: UserType) = _state.update { it.copy(userType = type) }
    fun onTermsToggle() = _state.update { it.copy(termsAccepted = !it.termsAccepted) }

    fun submit() {
        if (!validate()) return
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val s = state.value
            val result = authRepository.signUpWithEmail(
                name = s.name.trim(),
                email = s.email.trim(),
                password = s.password,
                userType = s.userType,
            )
            when (result) {
                is DataResult.Success -> _state.update { it.copy(isLoading = false, signedUp = true) }
                is DataResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                DataResult.Loading -> Unit
            }
        }
    }

    private fun validate(): Boolean {
        val s = state.value
        return when {
            s.name.isBlank() -> { _state.update { it.copy(error = "الاسم مطلوب") }; false }
            s.email.isBlank() || !s.email.contains("@") -> { _state.update { it.copy(error = "إيميل غير صحيح") }; false }
            s.password.length < 6 -> { _state.update { it.copy(error = "كلمة المرور 6 أحرف على الأقل") }; false }
            s.password != s.confirmPassword -> { _state.update { it.copy(error = "كلمتا المرور غير متطابقتين") }; false }
            !s.termsAccepted -> { _state.update { it.copy(error = "لازم توافق على الشروط") }; false }
            else -> true
        }
    }
}
