package com.yallakhedma.app.data.datasource

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow

class FirebaseAuthDataSource {

    private val auth get() = Firebase.auth

    val authState: Flow<FirebaseUser?> = auth.authStateChanged

    suspend fun signUpWithEmail(email: String, password: String): FirebaseUser? =
        auth.createUserWithEmailAndPassword(email, password).user

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser? =
        auth.signInWithEmailAndPassword(email, password).user

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
    }

    suspend fun signOut() {
        auth.signOut()
    }

    fun currentUid(): String? = auth.currentUser?.uid

    fun currentEmail(): String? = auth.currentUser?.email

    /** Firebase provider ids linked to the current account ("password", "google.com", …). */
    fun providerIds(): List<String> = auth.currentUser?.providerData?.map { it.providerId }.orEmpty()

    /**
     * Sends Firebase's built-in verification email (free on the Spark plan).
     * [languageCode] localizes the email template ("ar" / "en").
     */
    suspend fun sendEmailVerification(languageCode: String) {
        auth.languageCode = languageCode
        val user = auth.currentUser ?: error("No signed-in user")
        user.sendEmailVerification()
    }

    /** Refreshes the user from the server and returns whether the email is verified. */
    suspend fun reloadEmailVerified(): Boolean {
        val user = auth.currentUser ?: return false
        user.reload()
        return auth.currentUser?.isEmailVerified == true
    }

    suspend fun reauthenticateWithPassword(password: String) {
        val user = auth.currentUser ?: error("No signed-in user")
        val email = user.email ?: error("Account has no email")
        user.reauthenticate(EmailAuthProvider.credential(email, password))
    }

    suspend fun reauthenticateWithGoogle(idToken: String) {
        val user = auth.currentUser ?: error("No signed-in user")
        user.reauthenticate(GoogleAuthProvider.credential(idToken = idToken, accessToken = null))
    }
}
