package com.zoewave.probase.applications.photodo.db.entity.time

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity

/**
 * Entity for time budgeting at the category level.
 */
@Entity(
    tableName = "time_budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class TimeBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val budgetId: Long = 0,
    val categoryId: Long,
    val targetTimeMillis: Long,
    val period: String = "WEEKLY", // e.g., DAILY, WEEKLY, MONTHLY
    val lastModified: Long = System.currentTimeMillis()
)
