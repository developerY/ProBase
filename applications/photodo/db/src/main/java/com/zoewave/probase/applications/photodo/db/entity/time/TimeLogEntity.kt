package com.zoewave.probase.applications.photodo.db.entity.time

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity

/**
 * Internal entity for time tracking logs.
 * Linked to a TaskEntity via foreign key.
 */
@Entity(
    tableName = "time_logs",
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
internal data class TimeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0,
    val taskId: Long,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val note: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)
