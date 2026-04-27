package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun saveCategory(category: Category)
    suspend fun deleteCategory(id: String)
    suspend fun getCategoryById(id: String): Category?
    suspend fun getCategoryByName(name: String): Category?
    suspend fun initializeDefaultCategories()
}
