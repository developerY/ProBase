package com.zoewave.probase.features.calendar.data.db.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Isolated entity to track synchronization between task IDs and system calendar events.
 * Lives in the feature-specific database.
 */
@Entity(
    tableName = "calendar_sync",
    indices = [Index(value = ["taskId"])]
)
data class CalendarSyncEntity(
    @PrimaryKey
    val taskId: Long, // Logical link to TaskEntity in another module
    val calendarEventId: Long,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
