package com.yallakhedma.app.data.auth

/**
 * Maps a Firebase auth exception to a safe, user-facing Arabic message.
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
internal fun authErrorMessage(error: Throwable, generic: String): String {
    val name = error::class.simpleName ?: ""
    return when {
        name.contains("WeakPassword", ignoreCase = true) -> "كلمة المرور ضعيفة جداً"
        name.contains("UserCollision", ignoreCase = true) -> "هذا البريد الإلكتروني مسجّل بالفعل"
        name.contains("Network", ignoreCase = true) -> "تعذّر الاتصال، تحقق من الإنترنت"
        name.contains("TooManyRequests", ignoreCase = true) ->
            "محاولات كثيرة، حاول لاحقاً"
        // InvalidCredentials (wrong password / malformed) and InvalidUser
        // (no such account / disabled) deliberately collapse into the generic
        // message to avoid revealing which field was wrong.
        else -> generic
    }
}
