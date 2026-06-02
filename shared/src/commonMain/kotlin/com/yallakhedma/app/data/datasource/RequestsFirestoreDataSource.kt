package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.domain.model.ServiceRequest
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RequestsFirestoreDataSource {

    private val requests get() = Firebase.firestore.collection(COLLECTION)

    suspend fun create(request: ServiceRequest) {
        val doc = requests.document
        doc.set(request.copy(id = doc.id))
    }

    /** Pending requests for a provider, newest first (sorted client-side). */
    fun observePendingForProvider(providerId: String): Flow<List<ServiceRequest>> =
        requests
            .where { "providerId" equalTo providerId }
            .snapshots
            .map { snap ->
                snap.documents
                    .map { it.data(ServiceRequest.serializer()).copy(id = it.id) }
                    .filter { it.status == ServiceRequest.STATUS_PENDING }
                    .sortedByDescending { it.createdAt }
            }

    /** Every request belonging to a provider (any status), newest first. */
    fun observeAllForProvider(providerId: String): Flow<List<ServiceRequest>> =
        requests
            .where { "providerId" equalTo providerId }
            .snapshots
            .map { snap ->
                snap.documents
                    .map { it.data(ServiceRequest.serializer()).copy(id = it.id) }
                    .sortedByDescending { it.createdAt }
            }

    /** All requests the client created, newest first (sorted client-side). */
    fun observeForClient(clientId: String): Flow<List<ServiceRequest>> =
        requests
            .where { "clientId" equalTo clientId }
            .snapshots
            .map { snap ->
                snap.documents
                    .map { it.data(ServiceRequest.serializer()).copy(id = it.id) }
                    .sortedByDescending { it.createdAt }
            }

    suspend fun updateStatus(requestId: String, status: String) {
        requests.document(requestId).update("status" to status)
    }

    /** Marks a request as paid and stores a small snapshot of the card +
     *  discount used. `couponCode`/`couponLabel` may be blank if no coupon was
     *  applied; `paidAmount` is what was actually charged. */
    suspend fun markPaid(
        requestId: String,
        brand: String,
        last4: String,
        paidAt: Long,
        paidAmount: Double,
        couponCode: String,
        couponLabel: String,
    ) {
        requests.document(requestId).update(
            "paid" to true,
            "paidAt" to paidAt,
            "paymentMethodBrand" to brand,
            "paymentMethodLast4" to last4,
            "paidAmount" to paidAmount,
            "couponCode" to couponCode,
            "couponLabel" to couponLabel,
        )
    }

    companion object {
        private const val COLLECTION = "requests"
    }
}
