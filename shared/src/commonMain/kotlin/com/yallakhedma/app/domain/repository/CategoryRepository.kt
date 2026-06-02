package com.yallakhedma.app.domain.repository

import com.yallakhedma.app.domain.model.ServiceCategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<ServiceCategory>>
}
