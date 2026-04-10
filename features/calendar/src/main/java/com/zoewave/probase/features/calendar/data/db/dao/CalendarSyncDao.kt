package com.zoewave.probase.features.calendar.data.db.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.zoewave.probase.features.calendar.data.db.entity.CalendarSyncEntity

/**
 * Isolated DAO for calendar sync operations.
 */
@Dao
interface CalendarSyncDao {

    @Upsert
    suspend fun upsertCalendarSync(sync: CalendarSyncEntity)

    @Query("SELECT * FROM calendar_sync WHERE taskId = :taskId")
    suspend fun getCalendarSyncForTask(taskId: Long): CalendarSyncEntity?

    @Delete
    suspend fun deleteCalendarSync(sync: CalendarSyncEntity)
}
