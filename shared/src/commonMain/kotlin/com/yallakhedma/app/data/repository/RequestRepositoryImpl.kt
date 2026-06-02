package com.yallakhedma.app.data.repository

import com.yallakhedma.app.data.datasource.RequestsFirestoreDataSource
import com.yallakhedma.app.domain.model.ServiceRequest
import com.yallakhedma.app.domain.repository.RequestRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

class RequestRepositoryImpl(
    private val ds: RequestsFirestoreDataSource,
) : RequestRepository {

    override suspend fun create(request: ServiceRequest): DataResult<Unit> =
        runCatching {
            ds.create(request)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Request failed", it) }

    override fun observePendingForProvider(providerId: String): Flow<List<ServiceRequest>> =
        ds.observePendingForProvider(providerId)

    override fun observeAllForProvider(providerId: String): Flow<List<ServiceRequest>> =
        ds.observeAllForProvider(providerId)

    override fun observeForClient(clientId: String): Flow<List<ServiceRequest>> =
        ds.observeForClient(clientId)

    override suspend fun updateStatus(requestId: String, status: String): DataResult<Unit> =
        runCatching {
            ds.updateStatus(requestId, status)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Update failed", it) }

    override suspend fun markPaid(
        requestId: String,
        brand: String,
        last4: String,
        paidAmount: Double,
        couponCode: String,
        couponLabel: String,
    ): DataResult<Unit> =
        runCatching {
            ds.markPaid(
                requestId = requestId,
                brand = brand,
                last4 = last4,
                paidAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                paidAmount = paidAmount,
                couponCode = couponCode,
                couponLabel = couponLabel,
            )
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Payment failed", it) }
}
