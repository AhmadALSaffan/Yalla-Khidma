package com.yallakhedma.app.presentation.screens.auth.otp

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.auth.EmailOtpSender
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class OtpScreenModel(
    private val authRepository: AuthRepository,
    private val emailOtpSender: EmailOtpSender,
) : ScreenModel {

    private val _state = MutableStateFlow(OtpState())
    val state = _state.asStateFlow()

    private var generatedCode: String = ""

    init {
        sendNewCode()
    }

    fun onCodeChange(value: String) {
        // Keep digits only, max 4.
        val digits = value.filter { it.isDigit() }.take(CODE_LENGTH)
        _state.update { it.copy(code = digits, error = null) }
    }

    fun verify() {
        val entered = state.value.code
        if (entered.length < CODE_LENGTH) {
            _state.update { it.copy(error = "أدخل الرمز كاملاً") }
            return
        }
        if (entered != generatedCode) {
            _state.update { it.copy(error = "الرمز غير صحيح") }
            return
        }
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.markEmailVerified()) {
                is DataResult.Success -> _state.update { it.copy(isLoading = false, verified = true) }
                is DataResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                DataResult.Loading -> Unit
            }
        }
    }

    fun resend() {
        if (!state.value.canResend) return
        sendNewCode()
    }

    private fun sendNewCode() {
        generatedCode = (1..CODE_LENGTH)
            .map { Random.nextInt(0, 10) }
            .joinToString("")
        screenModelScope.launch {
            _state.update { it.copy(isSending = true, error = null, code = "") }
            val email = authRepository.currentUser.firstOrNull()?.email
            if (email.isNullOrBlank()) {
                _state.update { it.copy(isSending = false, error = "لا يوجد بريد إلكتروني للحساب") }
                return@launch
            }
            _state.update { it.copy(email = email) }
            val sent = emailOtpSender.sendCode(email, generatedCode)
            if (!sent) {
                _state.update {
                    it.copy(isSending = false, error = "تعذّر إرسال الرمز، حاول لاحقاً")
                }
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
