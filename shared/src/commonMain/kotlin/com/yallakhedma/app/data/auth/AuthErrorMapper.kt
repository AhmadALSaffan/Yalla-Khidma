package com.yallakhedma.app.data.auth

import com.yallakhedma.app.domain.util.ErrorCode

/**
 * Maps a Firebase auth exception to a safe, language-neutral [ErrorCode]
 * (plus a legacy Arabic message for screens not yet localized).
 *
 * Two goals:
 *  1. Never surface raw Firebase exception text (English, technical, leaks internals).
 *  2. Prevent account enumeration — sign-in failures must not reveal whether it
 *     was the email or the password that was wrong, so "no such user" and
 *     "wrong password" both fall through to [generic].
 *
 * Matching is by class *name* (not type import) so it stays robust across
 * GitLive / Firebase SDK version bumps.
 */
internal fun authErrorCode(error: Throwable, generic: ErrorCode): ErrorCode {
    val name = error::class.simpleName ?: ""
    return when {
        name.contains("WeakPassword", ignoreCase = true) -> ErrorCode.WeakPassword
        name.contains("UserCollision", ignoreCase = true) -> ErrorCode.EmailInUse
        name.contains("Network", ignoreCase = true) -> ErrorCode.Network
        name.contains("TooManyRequests", ignoreCase = true) -> ErrorCode.TooManyRequests
        // InvalidCredentials (wrong password / malformed) and InvalidUser
        // (no such account / disabled) deliberately collapse into the generic
        // code to avoid revealing which field was wrong.
        else -> generic
    }
}

internal fun authErrorMessage(error: Throwable, generic: String): String =
    when (authErrorCode(error, ErrorCode.Unknown)) {
        ErrorCode.WeakPassword -> "كلمة المرور ضعيفة جداً"
        ErrorCode.EmailInUse -> "هذا البريد الإلكتروني مسجّل بالفعل"
        ErrorCode.Network -> "تعذّر الاتصال، تحقق من الإنترنت"
        ErrorCode.TooManyRequests -> "محاولات كثيرة، حاول لاحقاً"
        else -> generic
    }
