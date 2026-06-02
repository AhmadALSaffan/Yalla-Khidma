package com.yallakhedma.app.presentation.screens.auth.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val signedIn: Boolean = false,
)
