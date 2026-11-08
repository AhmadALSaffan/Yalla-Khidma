package com.yallakhedma.app.presentation.screens.auth.signup

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.auth.SocialAuthClient
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.presentation.i18n.UiText
import com.yallakhedma.app.presentation.i18n.toUiText
import com.yallakhedma.app.presentation.screens.auth.welcome.GoogleAuthDelegate
import com.yallakhedma.app.util.AuthValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.error_email_invalid
import yallakhidma.shared.generated.resources.error_email_required
import yallakhidma.shared.generated.resources.error_name_required
import yallakhidma.shared.generated.resources.error_password_mismatch
import yallakhidma.shared.generated.resources.error_password_short
import yallakhidma.shared.generated.resources.error_password_weak
import yallakhidma.shared.generated.resources.error_terms_required

class SignUpScreenModel(
    private val authRepository: AuthRepository,
    socialAuthClient: SocialAuthClient,
) : ScreenModel {

    private val google = GoogleAuthDelegate(authRepository, socialAuthClient)
    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = null, error = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, emailError = null, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, passwordError = null, error = null) }
    fun onConfirmPasswordChange(value: String) =
        _state.update { it.copy(confirmPassword = value, confirmError = null, error = null) }
    fun togglePasswordVisibility() = _state.update { it.copy(passwordVisible = !it.passwordVisible) }

    /** Sets the type chosen on the preceding selection screen. */
    fun setInitialUserType(type: UserType) = _state.update { it.copy(userType = type) }
    fun onTermsToggle() = _state.update { it.copy(termsAccepted = !it.termsAccepted, error = null) }

    /** Fastest path: Google with the account type already chosen. */
    fun continueWithGoogle() {
        if (state.value.busy) return
        screenModelScope.launch {
            _state.update { it.copy(googleLoading = true, error = null) }
            when (val result = google.signIn(presetType = state.value.userType)) {
                is DataResult.Success -> _state.update { it.copy(googleLoading = false, signedUp = true) }
                is DataResult.Error -> _state.update { it.copy(googleLoading = false, error = result.toUiText()) }
                DataResult.Loading -> Unit
            }
        }
    }

    fun submit() {
        if (state.value.busy || !validate()) return
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
                is DataResult.Error -> _state.update { it.copy(isLoading = false, error = result.toUiText()) }
                DataResult.Loading -> Unit
            }
        }
    }

    private fun validate(): Boolean {
        val s = state.value
        val nameError = if (s.name.isBlank()) UiText.of(Res.string.error_name_required) else null
        val emailError = when {
            s.email.isBlank() -> UiText.of(Res.string.error_email_required)
            !AuthValidation.isValidEmail(s.email) -> UiText.of(Res.string.error_email_invalid)
            else -> null
        }
        val passwordError = when {
            s.password.length < AuthValidation.MIN_PASSWORD_LENGTH -> UiText.of(Res.string.error_password_short)
            !AuthValidation.isStrongPassword(s.password) -> UiText.of(Res.string.error_password_weak)
            else -> null
        }
        val confirmError = if (s.password != s.confirmPassword) UiText.of(Res.string.error_password_mismatch) else null
        val termsError = if (!s.termsAccepted) UiText.of(Res.string.error_terms_required) else null
        _state.update {
            it.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmError = confirmError,
                error = termsError,
            )
        }
        return listOf(nameError, emailError, passwordError, confirmError, termsError).all { it == null }
    }
}
