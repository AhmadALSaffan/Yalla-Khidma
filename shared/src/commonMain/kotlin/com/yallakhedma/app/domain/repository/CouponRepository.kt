package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.Coupon
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface CouponRepository {
    fun observeForProvider(providerId: String): Flow<List<Coupon>>
    suspend fun findByCode(providerId: String, code: String): Coupon?
    suspend fun add(coupon: Coupon): DataResult<Unit>
    suspend fun delete(id: String): DataResult<Unit>
}
