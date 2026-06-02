package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.domain.model.PaymentMethod
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PaymentMethodsFirestoreDataSource {

    private fun col(uid: String) =
        Firebase.firestore.collection("users").document(uid).collection("payment_methods")

    suspend fun add(uid: String, method: PaymentMethod): String {
        val doc = col(uid).document
        doc.set(method.copy(id = doc.id))
        return doc.id
    }

    fun observe(uid: String): Flow<List<PaymentMethod>> =
        col(uid).snapshots.map { snap ->
            snap.documents
                .map { it.data(PaymentMethod.serializer()).copy(id = it.id) }
                .sortedByDescending { it.createdAt }
        }

    suspend fun delete(uid: String, id: String) {
        col(uid).document(id).delete()
    }
}
