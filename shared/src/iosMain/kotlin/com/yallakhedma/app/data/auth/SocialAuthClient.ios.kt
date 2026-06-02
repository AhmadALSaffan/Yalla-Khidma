package com.yallakhedma.app.data.auth

/**
 * iOS implementation is intentionally a stub. The Google/Apple sign-in UI on iOS must run
 * on a UIViewController, so it's driven from Swift (see iosApp/iOSApp.swift). Swift obtains
 * the ID token and calls back into Kotlin via [AuthRepository.signInWithGoogleCredential] or
 * [AuthRepository.signInWithAppleCredential].
 */
actual class SocialAuthClient {
    actual suspend fun signInWithGoogle(): String? = null
}

actual val isAppleSignInAvailable: Boolean = true
actual val isGoogleSignInNative: Boolean = false
