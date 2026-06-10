package com.yallakhedma.app.util

/** Shared auth input validation so login and sign-up agree on the rules. */
object AuthValidation {

    const val MIN_PASSWORD_LENGTH = 8

    // Basic shape check: something@something.tld — keeps obviously-bad
    // addresses from ever reaching Firebase (and our OTP email).
    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isStrongPassword(password: String): Boolean =
        password.length >= MIN_PASSWORD_LENGTH &&
            password.any { it.isLetter() } &&
            password.any { it.isDigit() }
}
