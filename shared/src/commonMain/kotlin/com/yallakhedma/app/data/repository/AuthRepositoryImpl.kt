package com.yallakhedma.app.data.repository

import com.yallakhedma.app.data.auth.authErrorCode
import com.yallakhedma.app.data.auth.authErrorMessage
import com.yallakhedma.app.data.datasource.FirebaseAuthDataSource
import com.yallakhedma.app.data.datasource.ProvidersFirestoreDataSource
import com.yallakhedma.app.data.datasource.UserFirestoreDataSource
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.model.User
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.util.DataResult
import com.yallakhedma.app.domain.util.ErrorCode
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
            ?: return DataResult.Error("تعذّر إنشاء الحساب، حاول مرة أخرى", code = ErrorCode.SignUpFailed)
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
    }.getOrElse {
        failure(it, "تعذّر إنشاء الحساب، حاول مرة أخرى", ErrorCode.SignUpFailed)
    }

    override suspend fun signInWithEmail(email: String, password: String): DataResult<User> =
        runCatching {
            val fbUser = authDs.signInWithEmail(email, password)
                ?: return DataResult.Error(SIGN_IN_GENERIC, code = ErrorCode.InvalidCredentials)
            val profile = userDs.getUser(fbUser.uid)
                ?: return DataResult.Error(SIGN_IN_GENERIC, code = ErrorCode.InvalidCredentials)
            DataResult.Success(profile)
        }.getOrElse {
            // Generic on purpose — never reveal whether email or password was wrong.
            failure(it, SIGN_IN_GENERIC, ErrorCode.InvalidCredentials)
        }

    override suspend fun sendPasswordResetEmail(email: String): DataResult<Unit> =
        runCatching {
            authDs.sendPasswordResetEmail(email)
            DataResult.Success(Unit)
        }.getOrElse { e ->
            // Enumeration-safe: if the address simply has no account, still report
            // success so an attacker can't probe which emails are registered.
            // Surface real problems (network, malformed) with a clean message.
            val name = e::class.simpleName ?: ""
            if (name.contains("InvalidUser", ignoreCase = true)) {
                DataResult.Success(Unit)
            } else {
                failure(e, "تعذّر إرسال رابط الاستعادة", ErrorCode.ResetFailed)
            }
        }

    override suspend fun signOut(): DataResult<Unit> =
        runCatching {
            authDs.signOut()
            DataResult.Success(Unit)
        }.getOrElse { failure(it, "تعذّر تسجيل الخروج", ErrorCode.Unknown) }

    override suspend fun markEmailVerified(): DataResult<Unit> =
        runCatching {
            val uid = authDs.currentUid()
                ?: return DataResult.Error("لا يوجد مستخدم مسجّل الدخول", code = ErrorCode.NotSignedIn)
            userDs.markEmailVerified(uid)
            DataResult.Success(Unit)
        }.getOrElse { failure(it, "تعذّر تأكيد البريد", ErrorCode.SaveFailed) }

    override suspend fun markProviderProfileCompleted(): DataResult<Unit> =
        runCatching {
            val uid = authDs.currentUid()
                ?: return DataResult.Error("لا يوجد مستخدم مسجّل الدخول", code = ErrorCode.NotSignedIn)
            userDs.markProviderProfileCompleted(uid)
            DataResult.Success(Unit)
        }.getOrElse { failure(it, "تعذّر حفظ الملف", ErrorCode.SaveFailed) }

    override fun currentUid(): String? = authDs.currentUid()

    override suspend fun updateUserProfile(user: User): DataResult<Unit> =
        runCatching {
            userDs.upsertUser(user)
            DataResult.Success(Unit)
        }.getOrElse { failure(it, "تعذّر تحديث الملف", ErrorCode.SaveFailed) }

    override suspend fun signInWithGoogleCredential(
        idToken: String,
        defaultUserType: UserType,
    ): DataResult<User> = runCatching {
        val credential = GoogleAuthProvider.credential(idToken = idToken, accessToken = null)
        val fbUser = Firebase.auth.signInWithCredential(credential).user
            ?: return DataResult.Error(SOCIAL_GENERIC, code = ErrorCode.SignInFailed)
        upsertOrFetchProfile(
            uid = fbUser.uid,
            email = fbUser.email,
            displayName = fbUser.displayName,
            userType = defaultUserType,
        )
    }.getOrElse { failure(it, SOCIAL_GENERIC, ErrorCode.SignInFailed) }

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
            ?: return DataResult.Error(SOCIAL_GENERIC, code = ErrorCode.SignInFailed)
        upsertOrFetchProfile(
            uid = fbUser.uid,
            email = fbUser.email,
            displayName = fbUser.displayName,
            userType = defaultUserType,
        )
    }.getOrElse { failure(it, SOCIAL_GENERIC, ErrorCode.SignInFailed) }

    override suspend fun chooseUserType(type: UserType): DataResult<User> =
        runCatching {
            val uid = authDs.currentUid()
                ?: return DataResult.Error("لا يوجد مستخدم مسجّل الدخول", code = ErrorCode.NotSignedIn)
            val existing = userDs.getUser(uid)
                ?: return DataResult.Error("تعذّر حفظ الملف", code = ErrorCode.SaveFailed)
            val updated = existing.copy(userType = type, userTypeChosen = true)
            userDs.upsertUser(updated)
            if (type == UserType.Provider) {
                providersDs.upsertProvider(Provider(id = uid, name = updated.name, isFeatured = false))
            }
            DataResult.Success(updated)
        }.getOrElse { failure(it, "تعذّر حفظ الملف", ErrorCode.SaveFailed) }

    private fun failure(e: Throwable, genericMessage: String, genericCode: ErrorCode) =
        DataResult.Error(
            message = authErrorMessage(e, generic = genericMessage),
            cause = e,
            code = authErrorCode(e, generic = genericCode),
        )

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
                // Social sign-in doesn't tell us client vs provider — routing
                // sends the user to the account-type step before anything else.
                userTypeChosen = false,
            )
            userDs.upsertUser(profile)
            DataResult.Success(profile)
        }
    }

    private companion object {
        // Single generic credential error — keeps "no such user" and "wrong
        // password" indistinguishable so accounts can't be enumerated.
        const val SIGN_IN_GENERIC = "البريد الإلكتروني أو كلمة المرور غير صحيحة"
        const val SOCIAL_GENERIC = "تعذّر تسجيل الدخول، حاول مرة أخرى"
    }
}
