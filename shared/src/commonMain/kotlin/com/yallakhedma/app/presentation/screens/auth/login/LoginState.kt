package com.yallakhedma.app.presentation.screens.auth.login

import com.yallakhedma.app.presentation.i18n.UiText

data class LoginState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    /** Form-level error (wrong credentials, network, …). */
    val error: UiText? = null,
    val signedIn: Boolean = false,
)
