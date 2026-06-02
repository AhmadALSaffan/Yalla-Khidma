package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.PaymentMethod
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface PaymentMethodRepository {
    fun observe(uid: String): Flow<List<PaymentMethod>>
    suspend fun add(uid: String, method: PaymentMethod): DataResult<Unit>
    suspend fun delete(uid: String, id: String): DataResult<Unit>
}
