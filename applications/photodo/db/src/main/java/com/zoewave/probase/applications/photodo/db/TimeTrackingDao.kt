package com.zoewave.probase.applications.photodo.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.zoewave.probase.applications.photodo.db.entity.time.CalendarSyncEntity
import com.zoewave.probase.applications.photodo.db.entity.time.TimeBudgetEntity
import com.zoewave.probase.applications.photodo.db.entity.time.TimeLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for time tracking and budgeting operations.
 */
@Dao
interface TimeTrackingDao {

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

    // --- Calendar Sync Operations ---

    @Upsert
    suspend fun upsertCalendarSync(sync: CalendarSyncEntity)

    @Query("SELECT * FROM calendar_sync WHERE taskId = :taskId")
    suspend fun getCalendarSyncForTask(taskId: Long): CalendarSyncEntity?

    @Delete
    suspend fun deleteCalendarSync(sync: CalendarSyncEntity)
}
