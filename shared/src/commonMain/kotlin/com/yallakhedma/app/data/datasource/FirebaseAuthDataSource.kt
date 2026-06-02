package com.yallakhedma.app.data.datasource

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
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
}
