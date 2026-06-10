package com.yallakhedma.app.presentation.screens.auth.otp

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
 * Drives email verification entirely through the server. The 4-digit code is
 * generated, stored (hashed), and verified by Cloud Functions — this model only
 * triggers send/verify and reflects the result. All anti-abuse (expiry, attempt
 * cap, resend cooldown) is enforced server-side; the timer here is just UX.
 */
class OtpScreenModel(
    private val authRepository: AuthRepository,
    private val otpService: OtpService,
) : ScreenModel {

    private val _state = MutableStateFlow(OtpState())
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
            _state.update { it.copy(isLoading = true, error = null) }
            val ok = runCatching {
                otpService.verify(OtpService.PURPOSE_EMAIL_VERIFY, entered)
            }.getOrElse { e ->
                // Cloud Functions throw a FirebaseFunctionsException whose message
                // is the Arabic string we set server-side (wrong code, expired, …).
                _state.update { it.copy(isLoading = false, error = e.message ?: "تعذّر التحقق") }
                return@launch
            }
            if (ok) {
                // The server already flipped users/{uid}.emailVerified = true.
                _state.update { it.copy(isLoading = false, verified = true) }
            } else {
                _state.update { it.copy(isLoading = false, error = "الرمز غير صحيح") }
            }
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
            _state.update { it.copy(isSending = true, error = null, code = "") }
            val result = runCatching { otpService.send(OtpService.PURPOSE_EMAIL_VERIFY) }
            if (result.isFailure) {
                _state.update {
                    it.copy(
                        isSending = false,
                        error = result.exceptionOrNull()?.message ?: "تعذّر إرسال الرمز، حاول لاحقاً",
                    )
                }
                // Still start the timer so the user can retry after the cooldown.
                startTimer()
                return@launch
            }
            _state.update { it.copy(isSending = false) }
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

    data class OtpState(
        val code: String = "",
        val email: String = "",
        val isSending: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val verified: Boolean = false,
        val secondsLeft: Int = RESEND_SECONDS,
        val canResend: Boolean = false,
    )

    companion object {
        const val CODE_LENGTH = 4
        private const val RESEND_SECONDS = 59
    }
}
