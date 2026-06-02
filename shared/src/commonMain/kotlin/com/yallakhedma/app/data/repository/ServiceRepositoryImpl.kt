package com.yallakhedma.app.data.repository

import com.yallakhedma.app.data.datasource.ServicesFirestoreDataSource
import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.repository.ServiceRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

class ServiceRepositoryImpl(
    private val ds: ServicesFirestoreDataSource,
) : ServiceRepository {

    override fun observeNearby(limit: Int): Flow<List<Service>> =
        ds.observeNearby(limit)

    override fun observeAll(limit: Int): Flow<List<Service>> =
        ds.observeAll(limit)

    override fun observeByProvider(providerId: String): Flow<List<Service>> =
        ds.observeByProvider(providerId)

    override fun observeById(id: String): Flow<Service?> =
        ds.observeById(id)

    override suspend fun addService(service: Service): DataResult<Unit> =
        runCatching {
            ds.addService(service)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Add failed", it) }
}
