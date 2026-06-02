package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.domain.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserFirestoreDataSource {

    private val users get() = Firebase.firestore.collection(COLLECTION)

    suspend fun upsertUser(user: User) {
        users.document(user.id).set(user)
    }

    suspend fun getUser(userId: String): User? {
        val snap = users.document(userId).get()
        return if (snap.exists) snap.data(User.serializer()) else null
    }

    /** Re-fetches the user, flips emailVerified, and writes it back. */
    suspend fun markEmailVerified(userId: String) {
        val existing = getUser(userId) ?: return
        users.document(userId).set(existing.copy(emailVerified = true))
    }

    /** Re-fetches the user, flips providerProfileCompleted, and writes it back. */
    suspend fun markProviderProfileCompleted(userId: String) {
        val existing = getUser(userId) ?: return
        users.document(userId).set(existing.copy(providerProfileCompleted = true))
    }

    fun observeUser(userId: String): Flow<User?> =
        users.document(userId).snapshots.map { snap ->
            if (snap.exists) snap.data(User.serializer()) else null
        }

    companion object {
        private const val COLLECTION = "users"
    }
}
