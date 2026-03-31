package com.zoewave.probase.applications.photodo.db.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["projectId"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val taskId: Long = 0,
    val projectId: Long, // Foreign Key to the Project
    val text: String,
    val isChecked: Boolean = false,
    val sortOrder: Int = 0,
    var globalSyncId: String = UUID.randomUUID().toString(),
    var lastModified: Long = System.currentTimeMillis(),
    var assignedTo: String? = null,
    var estimatedCost: Double? = null,
    var actualCost: Double? = null
)
