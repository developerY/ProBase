package com.zoewave.probase.applications.photodo.db.entity.time

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity

/**
 * Entity to track synchronization between PhotoDo tasks and system calendar events.
 */
@Entity(
    tableName = "calendar_sync",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["taskId"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class CalendarSyncEntity(
    @PrimaryKey
    val taskId: Long,
    val calendarEventId: Long,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
