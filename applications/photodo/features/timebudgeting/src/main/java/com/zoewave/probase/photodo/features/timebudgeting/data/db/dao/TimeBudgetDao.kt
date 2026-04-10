package com.zoewave.probase.photodo.features.timebudgeting.data.db.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.zoewave.probase.photodo.features.timebudgeting.data.db.entity.TimeBudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Isolated DAO for time budgeting operations.
 */
@Dao
interface TimeBudgetDao {

    @Upsert
    suspend fun upsertTimeBudget(budget: TimeBudgetEntity): Long

    @Query("SELECT * FROM time_budgets WHERE categoryId = :categoryId")
    fun getTimeBudgetForCategory(categoryId: Long): Flow<TimeBudgetEntity?>

    @Query("SELECT * FROM time_budgets")
    fun getAllTimeBudgets(): Flow<List<TimeBudgetEntity>>

    @Delete
    suspend fun deleteTimeBudget(budget: TimeBudgetEntity)
}
