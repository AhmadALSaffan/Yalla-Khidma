package com.yallakhedma.app.data.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.functions.FirebaseFunctions
import dev.gitlive.firebase.functions.functions
import kotlinx.serialization.Serializable

/**
 * Server-side OTP, backed by the `sendOtp` / `verifyOtp` Cloud Functions.
 *
 * The code is generated, stored (hashed), and verified ONLY on the server, and
 * the SMTP credentials live in Secret Manager — so nothing sensitive ships in
 * the app binary and a tampered client can't read or fake the code.
 */
class OtpService {

    // Must match the region the functions are deployed to (see functions/src/index.ts).
    private val functions: FirebaseFunctions get() = Firebase.functions(REGION)

    /** Asks the server to generate + email a fresh code for [purpose]. */
    suspend fun send(purpose: String) {
        functions.httpsCallable("sendOtp").invoke(SendOtpRequest(purpose))
    }

    /** @return true only if the server confirms the code is correct + unexpired. */
    suspend fun verify(purpose: String, code: String): Boolean {
        val result = functions.httpsCallable("verifyOtp").invoke(VerifyOtpRequest(purpose, code))
        return result.data<VerifyOtpResponse>().verified
    }

    @Serializable
    private data class SendOtpRequest(val purpose: String)

    @Serializable
    private data class VerifyOtpRequest(val purpose: String, val code: String)

    @Serializable
    private data class VerifyOtpResponse(val verified: Boolean = false)

    companion object {
        private const val REGION = "us-central1"
        const val PURPOSE_EMAIL_VERIFY = "email_verify"
        const val PURPOSE_PROFILE_EDIT = "profile_edit"
    }
}
