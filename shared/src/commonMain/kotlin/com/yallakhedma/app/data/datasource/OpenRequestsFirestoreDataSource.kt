package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.domain.model.Offer
import com.yallakhedma.app.domain.model.OpenRequest
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.util.nowMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OpenRequestsFirestoreDataSource {

    private val db get() = Firebase.firestore
    private val requests get() = db.collection("openRequests")
    private val offers get() = db.collection("offers")
    private val bookings get() = db.collection("requests")

    suspend fun create(request: OpenRequest): String {
        val doc = requests.document
        doc.set(request.copy(id = doc.id, createdAt = nowMillis()))
        return doc.id
    }

    fun observeForClient(clientId: String): Flow<List<OpenRequest>> =
        requests.where { "clientId" equalTo clientId }.snapshots.map { snap ->
            snap.documents.map { it.data(OpenRequest.serializer()).copy(id = it.id) }.sortedByDescending { it.createdAt }
        }

    fun observeOpen(): Flow<List<OpenRequest>> =
        requests.where { "status" equalTo OpenRequest.STATUS_OPEN }.snapshots.map { snap ->
            snap.documents.map { it.data(OpenRequest.serializer()).copy(id = it.id) }.sortedByDescending { it.createdAt }
        }

    fun observe(requestId: String): Flow<OpenRequest?> =
        requests.document(requestId).snapshots.map { doc ->
            if (doc.exists) doc.data(OpenRequest.serializer()).copy(id = doc.id) else null
        }

    suspend fun close(requestId: String) {
        requests.document(requestId).update("status" to OpenRequest.STATUS_CLOSED)
    }

    /** Offers received by a client (all their requests). */
    fun observeOffersForClient(clientId: String): Flow<List<Offer>> =
        offers.where { "clientId" equalTo clientId }.snapshots.map { snap ->
            snap.documents.map { it.data(Offer.serializer()).copy(id = it.id) }.sortedByDescending { it.createdAt }
        }

    /** Offers a provider has sent. */
    fun observeOffersForProvider(providerId: String): Flow<List<Offer>> =
        offers.where { "providerId" equalTo providerId }.snapshots.map { snap ->
            snap.documents.map { it.data(Offer.serializer()).copy(id = it.id) }.sortedByDescending { it.createdAt }
        }

    suspend fun sendOffer(offer: Offer) {
        val doc = offers.document
        doc.set(offer.copy(id = doc.id, createdAt = nowMillis()))
        requests.document(offer.requestId).update("offersCount" to FieldValue.increment(1))
    }

    /**
     * Accepts [offer]: creates an accepted booking (so it flows into payment
     * like any other), marks the offer accepted, declines the rest and
     * assigns the request.
     */
    suspend fun acceptOffer(request: OpenRequest, offer: Offer, others: List<Offer>, booking: ServiceRequest) {
        val bookingDoc = bookings.document
        bookingDoc.set(booking.copy(id = bookingDoc.id, createdAt = nowMillis()))
        offers.document(offer.id).update("status" to Offer.STATUS_ACCEPTED)
        others.filter { it.id != offer.id && it.status == Offer.STATUS_PENDING }.forEach {
            runCatching { offers.document(it.id).update("status" to Offer.STATUS_DECLINED) }
        }
        requests.document(request.id).update(
            "status" to OpenRequest.STATUS_ASSIGNED,
            "acceptedOfferId" to offer.id,
        )
    }
}
