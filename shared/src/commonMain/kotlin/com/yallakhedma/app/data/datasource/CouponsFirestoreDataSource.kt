package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.domain.model.Coupon
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CouponsFirestoreDataSource {

    private val coupons get() = Firebase.firestore.collection(COLLECTION)

    /** Streams every coupon owned by the given provider, newest first. */
    fun observeForProvider(providerId: String): Flow<List<Coupon>> =
        coupons
            .where { "providerId" equalTo providerId }
            .snapshots
            .map { snap ->
                snap.documents
                    .map { it.data(Coupon.serializer()).copy(id = it.id) }
                    .sortedByDescending { it.createdAt }
            }

    /** Looks up a coupon by (provider, code). Code match is case-insensitive. */
    suspend fun findByCode(providerId: String, code: String): Coupon? {
        val normalized = code.trim().uppercase()
        if (normalized.isBlank() || providerId.isBlank()) return null
        val snap = coupons
            .where { "providerId" equalTo providerId }
            .where { "code" equalTo normalized }
            .get()
        return snap.documents
            .map { it.data(Coupon.serializer()).copy(id = it.id) }
            .firstOrNull { it.active }
    }

    suspend fun add(coupon: Coupon): String {
        val doc = coupons.document
        doc.set(coupon.copy(id = doc.id))
        return doc.id
    }

    suspend fun delete(id: String) {
        coupons.document(id).delete()
    }

    companion object {
        private const val COLLECTION = "coupons"
    }
}
