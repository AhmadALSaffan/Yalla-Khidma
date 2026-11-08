package com.yallakhedma.app.presentation.screens.auth.forgot

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

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: UiText? = null,
    val error: UiText? = null,
    /** Email the link was sent to — non-null switches the UI to the "check inbox" state. */
    val sentTo: String? = null,
)

class ForgotPasswordScreenModel(
    private val authRepository: AuthRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()

    fun prefill(email: String) {
        if (_state.value.email.isEmpty() && email.isNotEmpty()) _state.update { it.copy(email = email) }
    }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, emailError = null, error = null) }

    fun submit() {
        val email = state.value.email.trim()
        val emailError = when {
            email.isBlank() -> UiText.of(Res.string.error_email_required)
            !AuthValidation.isValidEmail(email) -> UiText.of(Res.string.error_email_invalid)
            else -> null
        }
        if (emailError != null) {
            _state.update { it.copy(emailError = emailError) }
            return
        }
        if (state.value.isLoading) return
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            // Enumeration-safe: the repository reports success for unknown emails too.
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is DataResult.Success -> _state.update { it.copy(isLoading = false, sentTo = email) }
                is DataResult.Error -> _state.update { it.copy(isLoading = false, error = result.toUiText()) }
                DataResult.Loading -> Unit
            }
        }
    }

    fun editEmail() = _state.update { it.copy(sentTo = null) }
}
