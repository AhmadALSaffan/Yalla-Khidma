package com.yallakhedma.app.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.yallakhedma.app.util.CurrentActivityHolder

actual class SocialAuthClient(
    private val appContext: Context,
    private val webClientId: String,
) {
    actual suspend fun signInWithGoogle(): String? {
        val activity = CurrentActivityHolder.activity ?: run {
            Log.w(TAG, "No current Activity — cannot present Credential Manager UI")
            return null
        }
        if (webClientId.isBlank() || webClientId.startsWith("YOUR_")) {
            Log.w(TAG, "google_web_client_id is unset — paste it into strings.xml")
            return null
        }

        val credentialManager = CredentialManager.create(appContext)

        // 1) Silent path: returning user with a previously-authorized Google account.
        tryGetCredential(
            cm = credentialManager,
            activity = activity,
            option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build(),
            label = "GetGoogleIdOption (silent)",
        )?.let { return it }

        // 2) Full sign-in flow: shows the account picker.
        return tryGetCredential(
            cm = credentialManager,
            activity = activity,
            option = GetSignInWithGoogleOption.Builder(webClientId).build(),
            label = "GetSignInWithGoogleOption (picker)",
        )
    }

    private suspend fun tryGetCredential(
        cm: CredentialManager,
        activity: Activity,
        option: androidx.credentials.CredentialOption,
        label: String,
    ): String? {
        return try {
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            val response = cm.getCredential(activity, request)
            extractIdToken(response).also {
                if (it == null) Log.w(TAG, "$label returned an unexpected credential type")
            }
        } catch (e: GetCredentialException) {
            Log.w(TAG, "$label failed: ${e::class.simpleName} — ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "$label crashed", e)
            null
        }
    }

    private fun extractIdToken(response: GetCredentialResponse): String? {
        val credential = response.credential
        return if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else null
    }

    private companion object {
        const val TAG = "SocialAuthClient"
    }
}

actual val isAppleSignInAvailable: Boolean = false
actual val isGoogleSignInNative: Boolean = true
