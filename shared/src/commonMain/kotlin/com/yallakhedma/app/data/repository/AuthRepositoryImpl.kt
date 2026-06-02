package com.yallakhedma.app.data.repository

import com.yallakhedma.app.data.datasource.FirebaseAuthDataSource
import com.yallakhedma.app.data.datasource.ProvidersFirestoreDataSource
import com.yallakhedma.app.data.datasource.UserFirestoreDataSource
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.User
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock

class AuthRepositoryImpl(
    private val authDs: FirebaseAuthDataSource,
    private val userDs: UserFirestoreDataSource,
    private val providersDs: ProvidersFirestoreDataSource,
) : AuthRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUser: Flow<User?> = authDs.authState.flatMapLatest { fbUser ->
        if (fbUser == null) flowOf(null) else userDs.observeUser(fbUser.uid)
    }

    override fun hasPersistedSession(): Boolean = authDs.currentUid() != null

    override suspend fun signUpWithEmail(
        name: String,
        email: String,
        password: String,
        userType: UserType,
    ): DataResult<User> = runCatching {
        val fbUser = authDs.signUpWithEmail(email, password)
            ?: return DataResult.Error("Sign up returned no user")
        val profile = User(
            id = fbUser.uid,
            email = email,
            name = name,
            userType = userType,
            createdAt = Clock.System.now().toEpochMilliseconds(),
        )
        userDs.upsertUser(profile)
        // Providers get a matching document in the `providers` collection
        // (doc id == uid) so they appear in listings and can be booked. The
        // provider fills in the rest of the profile (photo, bio, …) later.
        if (userType == UserType.Provider) {
            providersDs.upsertProvider(
                Provider(
                    id = fbUser.uid,
                    name = name,
                    isFeatured = false,
                ),
            )
        }
        DataResult.Success(profile)
    }.getOrElse { DataResult.Error(it.message ?: "Unknown error", it) }

    override suspend fun signInWithEmail(email: String, password: String): DataResult<User> =
        runCatching {
            val fbUser = authDs.signInWithEmail(email, password)
                ?: return DataResult.Error("Sign in returned no user")
            val profile = userDs.getUser(fbUser.uid)
                ?: return DataResult.Error("User profile missing")
            DataResult.Success(profile)
        }.getOrElse { DataResult.Error(it.message ?: "Unknown error", it) }

    override suspend fun sendPasswordResetEmail(email: String): DataResult<Unit> =
        runCatching {
            authDs.sendPasswordResetEmail(email)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Unknown error", it) }

    override suspend fun signOut(): DataResult<Unit> =
        runCatching {
            authDs.signOut()
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Unknown error", it) }

    override suspend fun markEmailVerified(): DataResult<Unit> =
        runCatching {
            val uid = authDs.currentUid()
                ?: return DataResult.Error("No signed-in user")
            userDs.markEmailVerified(uid)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Unknown error", it) }

    override suspend fun markProviderProfileCompleted(): DataResult<Unit> =
        runCatching {
            val uid = authDs.currentUid()
                ?: return DataResult.Error("No signed-in user")
            userDs.markProviderProfileCompleted(uid)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Unknown error", it) }

    override fun currentUid(): String? = authDs.currentUid()

    override suspend fun updateUserProfile(user: User): DataResult<Unit> =
        runCatching {
            userDs.upsertUser(user)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Update failed", it) }

    override suspend fun signInWithGoogleCredential(
        idToken: String,
        defaultUserType: UserType,
    ): DataResult<User> = runCatching {
        val credential = GoogleAuthProvider.credential(idToken = idToken, accessToken = null)
        val fbUser = Firebase.auth.signInWithCredential(credential).user
            ?: return DataResult.Error("Google sign-in returned no user")
        upsertOrFetchProfile(
            uid = fbUser.uid,
            email = fbUser.email,
            displayName = fbUser.displayName,
            userType = defaultUserType,
        )
    }.getOrElse { DataResult.Error(it.message ?: "Unknown error", it) }

    override suspend fun signInWithAppleCredential(
        idToken: String,
        rawNonce: String,
        defaultUserType: UserType,
    ): DataResult<User> = runCatching {
        val credential = OAuthProvider.credential(
            providerId = "apple.com",
            idToken = idToken,
            rawNonce = rawNonce,
            accessToken = null,
        )
        val fbUser = Firebase.auth.signInWithCredential(credential).user
            ?: return DataResult.Error("Apple sign-in returned no user")
        upsertOrFetchProfile(
            uid = fbUser.uid,
            email = fbUser.email,
            displayName = fbUser.displayName,
            userType = defaultUserType,
        )
    }.getOrElse { DataResult.Error(it.message ?: "Unknown error", it) }

    /** First-time social sign-in creates a profile; returning users get their existing one. */
    private suspend fun upsertOrFetchProfile(
        uid: String,
        email: String?,
        displayName: String?,
        userType: UserType,
    ): DataResult<User> {
        val existing = userDs.getUser(uid)
        return if (existing != null) {
            DataResult.Success(existing)
        } else {
            val profile = User(
                id = uid,
                email = email,
                name = displayName ?: email?.substringBefore('@') ?: "مستخدم",
                userType = userType,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                // Google/Apple already verified the email — skip our OTP step.
                emailVerified = true,
            )
            userDs.upsertUser(profile)
            DataResult.Success(profile)
        }
    }
}
