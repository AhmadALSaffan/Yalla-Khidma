package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.domain.model.Provider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProvidersFirestoreDataSource {

    private val providers get() = Firebase.firestore.collection(COLLECTION)

    /** Streams featured providers, capped at [limit]. */
    fun observeFeatured(limit: Int = 10): Flow<List<Provider>> =
        providers
            .where { "isFeatured" equalTo true }
            .limit(limit.toLong())
            .snapshots
            .map { snap ->
                snap.documents.map { doc ->
                    doc.data(Provider.serializer()).copy(id = doc.id)
                }
            }

    /** Streams all providers (for the "all providers" page), capped at [limit]. */
    fun observeAll(limit: Int = 100): Flow<List<Provider>> =
        providers
            .limit(limit.toLong())
            .snapshots
            .map { snap ->
                snap.documents.map { doc ->
                    doc.data(Provider.serializer()).copy(id = doc.id)
                }
            }

    /** Creates/updates a provider document at providers/{provider.id}. */
    suspend fun upsertProvider(provider: Provider) {
        providers.document(provider.id).set(provider)
    }

    /** Streams a single provider by id (null if it doesn't exist). */
    fun observeById(id: String): Flow<Provider?> =
        providers.document(id).snapshots.map { snap ->
            if (snap.exists) snap.data(Provider.serializer()).copy(id = id) else null
        }

    /** Increments the provider's bookingsCount by one (read-modify-write). */
    suspend fun incrementBookings(providerId: String) {
        val ref = providers.document(providerId)
        val snap = ref.get()
        if (!snap.exists) return
        val current = snap.data(Provider.serializer()).bookingsCount
        ref.update("bookingsCount" to (current + 1))
    }

    companion object {
        private const val COLLECTION = "providers"
    }
}
