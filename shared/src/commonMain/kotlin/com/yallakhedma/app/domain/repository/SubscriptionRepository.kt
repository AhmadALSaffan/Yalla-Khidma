package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.Subscription
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun observe(providerId: String): Flow<Subscription?>
    suspend fun save(subscription: Subscription): DataResult<Unit>
}
