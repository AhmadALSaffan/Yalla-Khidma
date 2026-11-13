package com.yallakhedma.app.data.datasource

import com.yallakhedma.app.domain.model.Subscription
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscriptionsFirestoreDataSource {

    private val subs get() = Firebase.firestore.collection("subscriptions")
    private val providers get() = Firebase.firestore.collection("providers")

    fun observe(providerId: String): Flow<Subscription?> =
        subs.document(providerId).snapshots.map { doc ->
            if (doc.exists) doc.data(Subscription.serializer()) else null
        }

    /** Saves the subscription and mirrors the tier onto the public provider listing. */
    suspend fun save(subscription: Subscription) {
        subs.document(subscription.providerId).set(subscription)
        runCatching {
            providers.document(subscription.providerId).update(
                "subscriptionTier" to if (subscription.isLive) subscription.tier else "",
            )
        }
    }
}
