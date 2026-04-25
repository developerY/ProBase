package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.database.CategoryDao
import com.zoewave.probase.seaweed.database.toDomain
import com.zoewave.probase.seaweed.database.toEntity
import com.zoewave.probase.seaweed.model.Category
import com.zoewave.probase.seaweed.model.SpendingType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {
    override fun getAllCategories(): Flow<List<Category>> =
        dao.getAllCategories().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveCategory(category: Category) {
        dao.insertCategory(category.toEntity())
    }

    override suspend fun deleteCategory(id: String) {
        dao.deleteCategory(id)
    }

    override suspend fun getCategoryById(id: String): Category? {
        return dao.getCategoryById(id)?.toDomain()
    }

    override suspend fun initializeDefaultCategories() {
        val existing = dao.getAllCategories().first()
        if (existing.isEmpty()) {
            val defaults = listOf(
                Category(UUID.randomUUID().toString(), "Rent", SpendingType.NEED),
                Category(UUID.randomUUID().toString(), "Groceries", SpendingType.NEED),
                Category(UUID.randomUUID().toString(), "Healthcare", SpendingType.NEED),
                Category(UUID.randomUUID().toString(), "Utilities", SpendingType.NEED),
                Category(UUID.randomUUID().toString(), "Netflix", SpendingType.WANT),
                Category(UUID.randomUUID().toString(), "Spotify", SpendingType.WANT),
                Category(UUID.randomUUID().toString(), "Dining Out", SpendingType.WANT),
                Category(UUID.randomUUID().toString(), "Shopping", SpendingType.WANT)
            )
            defaults.forEach { dao.insertCategory(it.toEntity()) }
        }
    }
}
