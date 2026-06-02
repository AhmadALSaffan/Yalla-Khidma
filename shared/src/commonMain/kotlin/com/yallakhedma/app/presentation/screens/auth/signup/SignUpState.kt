package com.yallakhedma.app.presentation.screens.auth.signup

import com.yallakhedma.app.domain.model.UserType

data class SignUpState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val userType: UserType = UserType.Client,
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val signedUp: Boolean = false,
)
