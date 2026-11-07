package com.yallakhedma.app.presentation.screens.auth.otp

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.auth.OtpService
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.presentation.i18n.UiText
import dev.gitlive.firebase.functions.FirebaseFunctionsException
import dev.gitlive.firebase.functions.FunctionsExceptionCode
import dev.gitlive.firebase.functions.code
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.error_generic
import yallakhidma.shared.generated.resources.error_network
import yallakhidma.shared.generated.resources.error_not_signed_in
import yallakhidma.shared.generated.resources.error_otp_expired
import yallakhidma.shared.generated.resources.error_otp_incomplete
import yallakhidma.shared.generated.resources.error_otp_request_new
import yallakhidma.shared.generated.resources.error_otp_send_failed
import yallakhidma.shared.generated.resources.error_otp_too_many
import yallakhidma.shared.generated.resources.error_otp_wrong
import yallakhidma.shared.generated.resources.error_otp_wrong_left

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
            _state.update { it.copy(error = UiText.of(Res.string.error_otp_incomplete)) }
            return
        }
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val ok = runCatching {
                otpService.verify(OtpService.PURPOSE_EMAIL_VERIFY, entered)
            }.getOrElse { e ->
                // Cloud Functions throw a FirebaseFunctionsException; map its
                // code (not the server's Arabic message) so errors follow the app language.
                _state.update { it.copy(isLoading = false, error = otpErrorText(e)) }
                return@launch
            }
            if (ok) {
                // The server already flipped users/{uid}.emailVerified = true.
                _state.update { it.copy(isLoading = false, verified = true) }
            } else {
                _state.update { it.copy(isLoading = false, error = UiText.of(Res.string.error_otp_wrong)) }
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
                        error = result.exceptionOrNull()?.let(::otpSendErrorText)
                            ?: UiText.of(Res.string.error_otp_send_failed),
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
        val error: UiText? = null,
        val verified: Boolean = false,
        val secondsLeft: Int = RESEND_SECONDS,
        val canResend: Boolean = false,
    )

    companion object {
        const val CODE_LENGTH = 4

        /** Maps verifyOtp failures (see functions/src/index.ts) to localized text. */
        fun otpErrorText(e: Throwable): UiText {
            val code = (e as? FirebaseFunctionsException)?.code ?: return networkOrGeneric(e)
            return when (code) {
                FunctionsExceptionCode.PERMISSION_DENIED -> {
                    // Server message ends with the remaining-attempts count.
                    val left = e.message?.let { Regex("""(\d+)\s*$""").find(it)?.groupValues?.get(1)?.toIntOrNull() }
                    if (left != null) UiText.of(Res.string.error_otp_wrong_left, left)
                    else UiText.of(Res.string.error_otp_wrong)
                }
                FunctionsExceptionCode.INVALID_ARGUMENT -> UiText.of(Res.string.error_otp_wrong)
                FunctionsExceptionCode.DEADLINE_EXCEEDED -> UiText.of(Res.string.error_otp_expired)
                FunctionsExceptionCode.NOT_FOUND -> UiText.of(Res.string.error_otp_request_new)
                FunctionsExceptionCode.RESOURCE_EXHAUSTED -> UiText.of(Res.string.error_otp_too_many)
                FunctionsExceptionCode.UNAUTHENTICATED -> UiText.of(Res.string.error_not_signed_in)
                FunctionsExceptionCode.UNAVAILABLE -> UiText.of(Res.string.error_network)
                else -> UiText.of(Res.string.error_generic)
            }
        }

        /** Maps sendOtp failures (cooldown, no email, …) to localized text. */
        fun otpSendErrorText(e: Throwable): UiText {
            val code = (e as? FirebaseFunctionsException)?.code ?: return networkOrGeneric(e, Res.string.error_otp_send_failed)
            return when (code) {
                FunctionsExceptionCode.RESOURCE_EXHAUSTED -> UiText.of(Res.string.error_otp_too_many)
                FunctionsExceptionCode.UNAUTHENTICATED -> UiText.of(Res.string.error_not_signed_in)
                FunctionsExceptionCode.UNAVAILABLE -> UiText.of(Res.string.error_network)
                else -> UiText.of(Res.string.error_otp_send_failed)
            }
        }

        private fun networkOrGeneric(
            e: Throwable,
            fallback: org.jetbrains.compose.resources.StringResource = Res.string.error_generic,
        ): UiText =
            if ((e::class.simpleName ?: "").contains("Network", ignoreCase = true)) UiText.of(Res.string.error_network)
            else UiText.of(fallback)
        private const val RESEND_SECONDS = 59
    }
}
