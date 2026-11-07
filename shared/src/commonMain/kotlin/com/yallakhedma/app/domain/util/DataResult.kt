package com.yallakhedma.app.domain.util

sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()

    /**
     * [message] is a legacy Arabic fallback. New UI should localize via [code]
     * (see presentation/i18n/ErrorText.kt) so errors follow the app language.
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val code: ErrorCode = ErrorCode.Unknown,
    ) : DataResult<Nothing>()

    data object Loading : DataResult<Nothing>()
}

/** Language-neutral error kinds the UI can translate. */
enum class ErrorCode {
    Unknown,
    Network,
    TooManyRequests,
    InvalidCredentials,
    EmailInUse,
    WeakPassword,
    SignUpFailed,
    SignInFailed,
    ResetFailed,
    NotSignedIn,
    SaveFailed,
    /** The user closed the Google/Apple account picker. */
    SocialCancelled,
}
