package com.zoewave.probase.photodo.features.timebudgeting.data.db.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Isolated entity for time budgeting at the category level.
 * Lives in the feature-specific database.
 */
@Entity(
    tableName = "time_budgets",
    indices = [Index(value = ["categoryId"])]
)
data class TimeBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val budgetId: Long = 0,
    val categoryId: Long, // Logical link to CategoryEntity in the main DB
    val targetTimeMillis: Long,
    val period: String = "WEEKLY", // e.g., DAILY, WEEKLY, MONTHLY
    val lastModified: Long = System.currentTimeMillis()
)
