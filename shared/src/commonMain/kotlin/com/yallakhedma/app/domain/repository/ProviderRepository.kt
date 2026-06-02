package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.Provider
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface ProviderRepository {
    fun observeFeatured(limit: Int = 10): Flow<List<Provider>>
    fun observeAll(limit: Int = 100): Flow<List<Provider>>
    fun observeById(id: String): Flow<Provider?>
    suspend fun incrementBookings(providerId: String): DataResult<Unit>
    suspend fun saveProfile(provider: Provider): DataResult<Unit>
}
