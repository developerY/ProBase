package com.zoewave.probase.applications.photodo.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_items",
    foreignKeys = [
        ForeignKey(
            entity = TaskListEntity::class,
            parentColumns = ["listId"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE // If List is deleted, delete its items
        )
    ],
    // ✅ ADD THIS: Prevents full table scans during cascade deletes/updates
    indices = [Index(value = ["listId"])]
)
data class TaskItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val listId: Long, // Foreign Key to the TaskList
    val text: String,
    val isChecked: Boolean = false,
    // Dormant V2 Feature: Drag-and-drop reordering
    val sortOrder: Int = 0,

    // Dormant V2 Feature: Cloud Synchronization & Cross-Platform compatibility
    var globalSyncId: String = java.util.UUID.randomUUID().toString(),
    var lastModified: Long = System.currentTimeMillis(), // Crucial for resolving sync conflicts

    // Dormant V2 Feature: Multi-user delegation
    var assignedTo: String? = null, // Could be a name or an email

    // Dormant V2 Feature: Project Budgeting
    var estimatedCost: Double? = null,
    var actualCost: Double? = null
)