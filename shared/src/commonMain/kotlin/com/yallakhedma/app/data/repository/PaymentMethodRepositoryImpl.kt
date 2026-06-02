package com.yallakhedma.app.data.repository

import com.yallakhedma.app.data.datasource.PaymentMethodsFirestoreDataSource
import com.yallakhedma.app.domain.model.PaymentMethod
import com.yallakhedma.app.domain.repository.PaymentMethodRepository
import com.yallakhedma.app.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

class PaymentMethodRepositoryImpl(
    private val ds: PaymentMethodsFirestoreDataSource,
) : PaymentMethodRepository {

    override fun observe(uid: String): Flow<List<PaymentMethod>> = ds.observe(uid)

    override suspend fun add(uid: String, method: PaymentMethod): DataResult<Unit> =
        runCatching {
            ds.add(uid, method)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Add failed", it) }

    override suspend fun delete(uid: String, id: String): DataResult<Unit> =
        runCatching {
            ds.delete(uid, id)
            DataResult.Success(Unit)
        }.getOrElse { DataResult.Error(it.message ?: "Delete failed", it) }
}
