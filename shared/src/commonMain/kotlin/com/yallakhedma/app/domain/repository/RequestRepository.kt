package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface RequestRepository {
    suspend fun create(request: ServiceRequest): DataResult<Unit>
    fun observePendingForProvider(providerId: String): Flow<List<ServiceRequest>>
    fun observeAllForProvider(providerId: String): Flow<List<ServiceRequest>>
    fun observeForClient(clientId: String): Flow<List<ServiceRequest>>
    suspend fun updateStatus(requestId: String, status: String): DataResult<Unit>
    suspend fun markPaid(
        requestId: String,
        brand: String,
        last4: String,
        paidAmount: Double,
        couponCode: String,
        couponLabel: String,
    ): DataResult<Unit>
}
