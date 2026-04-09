package com.zoewave.probase.seaweed.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetTargetDao {
    @Query("SELECT * FROM budget_targets")
    fun getAllBudgets(): Flow<List<BudgetTargetEntity>>

    @Query("SELECT * FROM budget_targets WHERE categoryName = :categoryName")
    fun getBudget(categoryName: String): Flow<BudgetTargetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBudget(budget: BudgetTargetEntity)

    @Query("DELETE FROM budget_targets WHERE categoryName = :categoryName")
    suspend fun deleteBudget(categoryName: String)
}
