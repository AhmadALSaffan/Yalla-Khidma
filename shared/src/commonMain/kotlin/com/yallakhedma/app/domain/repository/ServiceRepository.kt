package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.Service
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {
    fun observeNearby(limit: Int = 10): Flow<List<Service>>
    fun observeAll(limit: Int = 100): Flow<List<Service>>
    fun observeByProvider(providerId: String): Flow<List<Service>>
    fun observeById(id: String): Flow<Service?>
    suspend fun addService(service: Service): DataResult<Unit>
}
