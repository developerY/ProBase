package com.zoewave.probase.applications.photodo.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.zoewave.probase.applications.photodo.db.entity.time.TimeLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Internal DAO for time tracking operations.
 */
@Dao
internal interface TimeTrackingDao {

    @Upsert
    suspend fun upsertTimeLog(log: TimeLogEntity): Long

    @Query("SELECT * FROM time_logs WHERE taskId = :taskId ORDER BY startTimeMillis DESC")
    fun getTimeLogsForTask(taskId: Long): Flow<List<TimeLogEntity>>

    @Delete
    suspend fun deleteTimeLog(log: TimeLogEntity)
}
