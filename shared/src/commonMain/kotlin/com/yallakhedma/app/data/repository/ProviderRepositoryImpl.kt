package com.yallakhedma.app.data.repository

import com.yallakhedma.app.data.datasource.ProvidersFirestoreDataSource
import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.repository.ProviderRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

class ProviderRepositoryImpl(
    private val ds: ProvidersFirestoreDataSource,
) : ProviderRepository {

    override fun observeFeatured(limit: Int): Flow<List<Provider>> =
        ds.observeFeatured(limit)

    override fun observeAll(limit: Int): Flow<List<Provider>> =
        ds.observeAll(limit)

    override fun observeById(id: String): Flow<Provider?> =
        ds.observeById(id)

    override suspend fun incrementBookings(providerId: String): DataResult<Unit> =
        runCatching {
            ds.incrementBookings(providerId)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Booking failed", it) }

    override suspend fun saveProfile(provider: Provider): DataResult<Unit> =
        runCatching {
            ds.upsertProvider(provider)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Save failed", it) }
}
