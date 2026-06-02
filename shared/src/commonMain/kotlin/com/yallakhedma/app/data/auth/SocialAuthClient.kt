package com.yallakhedma.app.data.auth

/**
 * Retrieves OAuth ID tokens from the platform's native sign-in flows.
 * Implementations:
 *  - Android: Credential Manager + Google ID
 *  - iOS: stubs returning null. iOS sign-in is driven from Swift (see iosApp/iOSApp.swift)
 *    which calls back into Kotlin via [AuthRepository.signInWithGoogleCredential] /
 *    [AuthRepository.signInWithAppleCredential] with the token it obtained.
 */
expect class SocialAuthClient {
    /** @return Google ID token, or null if the user cancelled or no Google account is available. */
    suspend fun signInWithGoogle(): String?
}

/** True on platforms where Apple sign-in is required/available (iOS). */
expect val isAppleSignInAvailable: Boolean

/** True on platforms where Google sign-in can be triggered from Kotlin alone (Android). */
expect val isGoogleSignInNative: Boolean
