package com.yallakhedma.app.presentation.screens.auth.verify

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.yallakhedma.app.data.local.AppPreferences
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.presentation.i18n.LanguageManager
import com.yallakhedma.app.presentation.i18n.UiText
import com.yallakhedma.app.presentation.i18n.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import yallakhidma.shared.generated.resources.Res
import yallakhidma.shared.generated.resources.verify_not_yet
import yallakhidma.shared.generated.resources.verify_sent

/**
 * Email verification with Firebase Auth's built-in link email (works on the
 * free Spark plan — no Cloud Functions). Polls quietly so the screen moves on
 * by itself as soon as the user taps the link.
 */
class EmailVerificationScreenModel(
    private val authRepository: AuthRepository,
    private val languageManager: LanguageManager,
    private val prefs: AppPreferences,
) : ScreenModel {

    data class State(
        val email: String = "",
        val sending: Boolean = false,
        val checking: Boolean = false,
        val verified: Boolean = false,
        val secondsLeft: Int = 0,
        val info: UiText? = null,
        val error: UiText? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()
    private var timerJob: Job? = null

    init {
        screenModelScope.launch {
            authRepository.currentUser.firstOrNull()?.email?.let { e -> _state.update { it.copy(email = e) } }
        }
        val sinceLast = Clock.System.now().toEpochMilliseconds() - prefs.lastVerificationEmailAt
        if (sinceLast > RECENT_MS) send(initial = true) else startCooldown(((RECENT_MS - sinceLast) / 1000).toInt().coerceAtMost(RESEND_SECONDS))
        // Background polling: detect the click without the user pressing anything.
        screenModelScope.launch {
            while (isActive && !state.value.verified) {
                delay(POLL_MS)
                val r = authRepository.refreshEmailVerified()
                if (r is DataResult.Success && r.data) _state.update { it.copy(verified = true) }
            }
        }
    }

    fun resend() {
        if (state.value.secondsLeft > 0 || state.value.sending) return
        send(initial = false)
    }

    /** "I've verified" — check right now and explain if it isn't done yet. */
    fun checkNow() {
        if (state.value.checking) return
        screenModelScope.launch {
            _state.update { it.copy(checking = true, error = null, info = null) }
            when (val r = authRepository.refreshEmailVerified()) {
                is DataResult.Success -> _state.update {
                    if (r.data) it.copy(checking = false, verified = true)
                    else it.copy(checking = false, error = UiText.of(Res.string.verify_not_yet))
                }
                is DataResult.Error -> _state.update { it.copy(checking = false, error = r.toUiText()) }
                DataResult.Loading -> Unit
            }
        }
    }

    private fun send(initial: Boolean) {
        screenModelScope.launch {
            _state.update { it.copy(sending = true, error = null, info = null) }
            when (val r = authRepository.sendEmailVerification(languageManager.language.value.code)) {
                is DataResult.Success -> {
                    prefs.lastVerificationEmailAt = Clock.System.now().toEpochMilliseconds()
                    _state.update { it.copy(sending = false, info = if (initial) null else UiText.of(Res.string.verify_sent)) }
                }
                is DataResult.Error -> _state.update { it.copy(sending = false, error = r.toUiText()) }
                DataResult.Loading -> Unit
            }
            startCooldown()
        }
    }

    private fun startCooldown(seconds: Int = RESEND_SECONDS) {
        timerJob?.cancel()
        timerJob = screenModelScope.launch {
            _state.update { it.copy(secondsLeft = seconds) }
            while (state.value.secondsLeft > 0) {
                delay(1000)
                _state.update { it.copy(secondsLeft = it.secondsLeft - 1) }
            }
        }
    }

    private companion object {
        const val RESEND_SECONDS = 60
        const val POLL_MS = 4000L
        const val RECENT_MS = 10 * 60 * 1000L
    }
}
