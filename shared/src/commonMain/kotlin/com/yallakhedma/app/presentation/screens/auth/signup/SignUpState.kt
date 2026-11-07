package com.yallakhedma.app.presentation.screens.auth.signup

import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.presentation.i18n.UiText

data class SignUpState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val userType: UserType = UserType.Client,
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val googleLoading: Boolean = false,
    val nameError: UiText? = null,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val confirmError: UiText? = null,
    val error: UiText? = null,
    val signedUp: Boolean = false,
) {
    val busy: Boolean get() = isLoading || googleLoading
}
