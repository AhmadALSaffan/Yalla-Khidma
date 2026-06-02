package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.domain.model.ServiceCategory
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoriesFirestoreDataSource {

    private val categories get() = Firebase.firestore.collection(COLLECTION)

    /**
     * Streams all categories, sorted client-side by `order`. We sort in code
     * (rather than Firestore orderBy) so a document missing the `order` field
     * is still included instead of being dropped from an ordered query.
     */
    fun observeAll(): Flow<List<ServiceCategory>> =
        categories.snapshots.map { snap ->
            snap.documents
                .map { doc -> doc.data(ServiceCategory.serializer()).copy(id = doc.id) }
                .sortedBy { it.order }
        }

    companion object {
        private const val COLLECTION = "categories"
    }
}
