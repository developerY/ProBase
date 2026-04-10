package com.zoewave.probase.applications.photodo.db.repo.time

import com.zoewave.probase.applications.photodo.db.entity.time.TimeBudgetEntity
import com.zoewave.probase.applications.photodo.db.entity.time.TimeLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for time tracking logic.
 */
interface TimeTrackingRepository {
    suspend fun saveTimeLog(log: TimeLogEntity): Long
    fun getTimeLogsForTask(taskId: Long): Flow<List<TimeLogEntity>>
    suspend fun deleteTimeLog(log: TimeLogEntity)

    // --- Time Budget Operations ---
    suspend fun saveTimeBudget(budget: TimeBudgetEntity): Long
    fun getTimeBudgetForCategory(categoryId: Long): Flow<TimeBudgetEntity?>
    fun getAllTimeBudgets(): Flow<List<TimeBudgetEntity>>
    suspend fun deleteTimeBudget(budget: TimeBudgetEntity)
}
