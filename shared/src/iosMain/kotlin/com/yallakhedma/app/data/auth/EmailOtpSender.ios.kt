package com.yallakhedma.app.data.auth

/**
 * iOS stub. JavaMail is JVM-only; iOS email OTP will go through a server-side
 * sender (Cloud Function) later. Returns false so the UI surfaces a clear error.
 */
actual class EmailOtpSender {
    actual suspend fun sendCode(toEmail: String, code: String): Boolean = false
}
