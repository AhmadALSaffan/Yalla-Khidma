package com.yallakhedma.app.data.repository

import com.yallakhedma.app.data.datasource.CouponsFirestoreDataSource
import com.yallakhedma.app.domain.model.Coupon
import com.yallakhedma.app.domain.repository.CouponRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

class CouponRepositoryImpl(
    private val ds: CouponsFirestoreDataSource,
) : CouponRepository {

    override fun observeForProvider(providerId: String): Flow<List<Coupon>> =
        ds.observeForProvider(providerId)

    override suspend fun findByCode(providerId: String, code: String): Coupon? =
        runCatching { ds.findByCode(providerId, code) }.getOrNull()

    override suspend fun add(coupon: Coupon): DataResult<Unit> =
        runCatching {
            ds.add(coupon)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Add failed", it) }

    override suspend fun delete(id: String): DataResult<Unit> =
        runCatching {
            ds.delete(id)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Delete failed", it) }
}
