package com.yallakhedma.app.data.auth

/**
 * Sends a one-time verification code to an email address.
 *
 * Android: uses JavaMail (Jakarta Mail) over SMTP — credentials are injected
 * from the platform module. iOS: stub for now (returns false) until a proper
 * server-side sender (Cloud Function) is wired up.
 *
 * NOTE: client-side SMTP embeds the sender credentials in the app binary,
 * which is fine for testing but should move to a backend before launch.
 */
expect class EmailOtpSender {
    /** @return true if the email was dispatched successfully. */
    suspend fun sendCode(toEmail: String, code: String): Boolean
}
