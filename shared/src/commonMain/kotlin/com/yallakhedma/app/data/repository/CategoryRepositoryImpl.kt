package com.yallakhedma.app.data.repository

import com.yallakhedma.app.data.datasource.CategoriesFirestoreDataSource
import com.yallakhedma.app.domain.model.ServiceCategory
import com.yallakhedma.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(
    private val ds: CategoriesFirestoreDataSource,
) : CategoryRepository {

    override fun observeAll(): Flow<List<ServiceCategory>> = ds.observeAll()
}
