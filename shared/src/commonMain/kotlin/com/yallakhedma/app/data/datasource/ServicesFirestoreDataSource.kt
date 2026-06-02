package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.domain.model.Service
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServicesFirestoreDataSource {

    private val services get() = Firebase.firestore.collection(COLLECTION)

    /** Streams services capped at [limit]. Real geo-filtering arrives later. */
    fun observeNearby(limit: Int = 10): Flow<List<Service>> = query(limit)

    /** Streams all services (for the "all services" page), capped at [limit]. */
    fun observeAll(limit: Int = 100): Flow<List<Service>> = query(limit)

    /** Creates a new service document (auto-id) and returns its id. */
    suspend fun addService(service: Service): String {
        val doc = services.document
        doc.set(service.copy(id = doc.id))
        return doc.id
    }

    /** Streams a single service by id (or null if the doc doesn't exist). */
    fun observeById(id: String): Flow<Service?> =
        services.document(id).snapshots.map { snap ->
            if (snap.exists) snap.data(Service.serializer()).copy(id = snap.id) else null
        }

    /** Streams services belonging to a given provider. */
    fun observeByProvider(providerId: String): Flow<List<Service>> =
        services
            .where { "providerId" equalTo providerId }
            .snapshots
            .map { snap ->
                snap.documents.map { doc ->
                    doc.data(Service.serializer()).copy(id = doc.id)
                }
            }

    private fun query(limit: Int): Flow<List<Service>> =
        services
            .limit(limit.toLong())
            .snapshots
            .map { snap ->
                snap.documents.map { doc ->
                    doc.data(Service.serializer()).copy(id = doc.id)
                }
            }

    companion object {
        private const val COLLECTION = "services"
    }
}
