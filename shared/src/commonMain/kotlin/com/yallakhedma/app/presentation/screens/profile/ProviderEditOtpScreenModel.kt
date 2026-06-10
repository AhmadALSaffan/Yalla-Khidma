package com.yallakhedma.app.presentation.screens.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.auth.OtpService
import com.yallakhedma.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Server-backed OTP gate before a provider edits their profile. Generation and
 * verification happen in Cloud Functions (purpose = "profile_edit"); this model
 * only triggers send/verify and reflects the result.
 */
class ProviderEditOtpScreenModel(
    private val authRepository: AuthRepository,
    private val otpService: OtpService,
) : ScreenModel {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        loadEmail()
        sendCode()
    }

    fun onCodeChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(CODE_LENGTH)
        _state.update { it.copy(code = digits, error = null) }
    }

    fun verify() {
        val entered = state.value.code
        if (entered.length < CODE_LENGTH) {
            _state.update { it.copy(error = "أدخل الرمز كاملاً") }
            return
        }
        screenModelScope.launch {
            val ok = runCatching {
                otpService.verify(OtpService.PURPOSE_PROFILE_EDIT, entered)
            }.getOrElse { e ->
                _state.update { it.copy(error = e.message ?: "تعذّر التحقق") }
                return@launch
            }
            if (ok) _state.update { it.copy(verified = true) }
            else _state.update { it.copy(error = "الرمز غير صحيح") }
        }
    }

    fun resend() {
        if (!state.value.canResend) return
        sendCode()
    }

    private fun loadEmail() {
        screenModelScope.launch {
            val email = authRepository.currentUser.firstOrNull()?.email
            if (!email.isNullOrBlank()) _state.update { it.copy(email = email) }
        }
    }

    private fun sendCode() {
        screenModelScope.launch {
            _state.update { it.copy(sending = true, error = null, code = "") }
            val result = runCatching { otpService.send(OtpService.PURPOSE_PROFILE_EDIT) }
            if (result.isFailure) {
                _state.update {
                    it.copy(sending = false, error = result.exceptionOrNull()?.message ?: "تعذّر إرسال الرمز")
                }
                startTimer()
                return@launch
            }
            _state.update { it.copy(sending = false) }
            startTimer()
        }
    }

    private fun startTimer() {
        screenModelScope.launch {
            _state.update { it.copy(secondsLeft = RESEND_SECONDS, canResend = false) }
            while (state.value.secondsLeft > 0) {
                delay(1000)
                _state.update { it.copy(secondsLeft = it.secondsLeft - 1) }
            }
            _state.update { it.copy(canResend = true) }
        }
    }

    data class State(
        val code: String = "",
        val email: String = "",
        val sending: Boolean = false,
        val verified: Boolean = false,
        val error: String? = null,
        val secondsLeft: Int = RESEND_SECONDS,
        val canResend: Boolean = false,
    )

    companion object {
        const val CODE_LENGTH = 4
        private const val RESEND_SECONDS = 59
    }
}
