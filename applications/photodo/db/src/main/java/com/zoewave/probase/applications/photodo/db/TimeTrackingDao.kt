package com.zoewave.probase.applications.photodo.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.zoewave.probase.applications.photodo.db.entity.time.TimeBudgetEntity
import com.zoewave.probase.applications.photodo.db.entity.time.TimeLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Internal DAO for time tracking and budgeting operations.
 */
@Dao
internal interface TimeTrackingDao {

    // --- Time Log Operations ---

    @Upsert
    suspend fun upsertTimeLog(log: TimeLogEntity): Long

    @Query("SELECT * FROM time_logs WHERE taskId = :taskId ORDER BY startTimeMillis DESC")
    fun getTimeLogsForTask(taskId: Long): Flow<List<TimeLogEntity>>

    @Delete
    suspend fun deleteTimeLog(log: TimeLogEntity)

    // --- Time Budget Operations ---

    @Upsert
    suspend fun upsertTimeBudget(budget: TimeBudgetEntity): Long

    @Query("SELECT * FROM time_budgets WHERE categoryId = :categoryId")
    fun getTimeBudgetForCategory(categoryId: Long): Flow<TimeBudgetEntity?>

    @Query("SELECT * FROM time_budgets")
    fun getAllTimeBudgets(): Flow<List<TimeBudgetEntity>>

    @Delete
    suspend fun deleteTimeBudget(budget: TimeBudgetEntity)
}
