package com.tvrgs.domain.repository

import com.tvrgs.domain.model.Category
import com.tvrgs.domain.model.ContentType
import com.tvrgs.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(providerId: Long): Flow<List<Category>>
    suspend fun setCategoryProtection(
        providerId: Long,
        categoryId: Long,
        type: ContentType,
        isProtected: Boolean
    ): Result<Unit>
}
