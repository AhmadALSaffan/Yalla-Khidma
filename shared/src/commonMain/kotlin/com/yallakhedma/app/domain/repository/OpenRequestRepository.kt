package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.Offer
import com.yallakhedma.app.domain.model.OpenRequest
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface OpenRequestRepository {
    suspend fun create(request: OpenRequest): DataResult<String>
    fun observeForClient(clientId: String): Flow<List<OpenRequest>>
    fun observeOpen(): Flow<List<OpenRequest>>
    fun observe(requestId: String): Flow<OpenRequest?>
    suspend fun close(requestId: String): DataResult<Unit>
    fun observeOffersForClient(clientId: String): Flow<List<Offer>>
    fun observeOffersForProvider(providerId: String): Flow<List<Offer>>
    suspend fun sendOffer(offer: Offer): DataResult<Unit>
    suspend fun acceptOffer(request: OpenRequest, offer: Offer, others: List<Offer>, booking: ServiceRequest): DataResult<Unit>
}
