package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.User
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentUser: Flow<User?>

    /**
     * Synchronously checks whether Firebase has a persisted auth session.
     * Used by the splash screen to decide whether to wait for the user
     * profile to load or route straight to login.
     */
    fun hasPersistedSession(): Boolean

    suspend fun signUpWithEmail(
        name: String,
        email: String,
        password: String,
        userType: UserType,
    ): DataResult<User>

    suspend fun signInWithEmail(email: String, password: String): DataResult<User>

    suspend fun sendPasswordResetEmail(email: String): DataResult<Unit>

    suspend fun signOut(): DataResult<Unit>

    /** Marks the currently signed-in user's email as verified (after OTP). */
    suspend fun markEmailVerified(): DataResult<Unit>

    /** Marks the provider's profile as completed (after the setup form). */
    suspend fun markProviderProfileCompleted(): DataResult<Unit>

    /** Updates the signed-in user's profile fields. */
    suspend fun updateUserProfile(user: User): DataResult<Unit>

    /** The currently signed-in user's id, or null. */
    fun currentUid(): String?

    /**
     * Sign in with a Google ID token already obtained natively
     * (Android: Credential Manager; iOS: GoogleSignIn SDK in Swift).
     */
    suspend fun signInWithGoogleCredential(
        idToken: String,
        defaultUserType: UserType = UserType.Client,
    ): DataResult<User>

    /**
     * Sign in with an Apple ID token + raw nonce already obtained natively
     * (iOS: ASAuthorizationAppleIDProvider in Swift).
     */
    suspend fun signInWithAppleCredential(
        idToken: String,
        rawNonce: String,
        defaultUserType: UserType = UserType.Client,
    ): DataResult<User>

    // TODO: phone OTP. Phone auth needs platform-specific wiring (Android: Activity context
    // for PhoneAuthProvider; iOS: APNs via Firebase).
}
