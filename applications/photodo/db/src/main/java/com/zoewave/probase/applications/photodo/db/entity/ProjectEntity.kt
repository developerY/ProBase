package com.zoewave.probase.applications.photodo.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val projectId: Long = 0,
    val categoryId: Long,
    var name: String,
    var notes: String? = null,
    var status: String = "To-Do",
    val isFavorite: Boolean = false,
    val isUrgent: Boolean = false,
    var priority: Int = 0,
    val creationDate: Long = System.currentTimeMillis(),
    var dueDate: Long? = null,
    var isAlarmEnabled: Boolean = false,
    val isArchived: Boolean = false,
    var globalSyncId: String = UUID.randomUUID().toString(),
    var lastModified: Long = System.currentTimeMillis()
)
