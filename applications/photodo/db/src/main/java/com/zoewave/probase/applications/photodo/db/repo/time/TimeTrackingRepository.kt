package com.zoewave.probase.applications.photodo.db.repo.time

import com.zoewave.probase.applications.photodo.db.entity.time.TimeLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Internal repository for time tracking logic.
 */
internal interface TimeTrackingRepository {
    suspend fun saveTimeLog(log: TimeLogEntity): Long
    fun getTimeLogsForTask(taskId: Long): Flow<List<TimeLogEntity>>
    suspend fun deleteTimeLog(log: TimeLogEntity)
}
