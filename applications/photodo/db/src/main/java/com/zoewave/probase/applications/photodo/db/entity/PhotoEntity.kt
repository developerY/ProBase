package com.zoewave.probase.applications.photodo.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = TaskListEntity::class, // Corrected: Points to the new TaskListEntity
            parentColumns = ["listId"],      // Corrected: Points to the new primary key
            childColumns = ["listId"],       // Corrected: The foreign key column in this table
            onDelete = ForeignKey.CASCADE
        )
    ],
    // ✅ ADD THIS: Prevents full table scans during cascade deletes/updates
    indices = [Index(value = ["listId"])]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val photoId: Long = 0,
    val listId: Long, // Corrected: Renamed from taskId to listId
    val photoUri: String,
    val caption: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    // Dormant V2 Feature: Map view and location-based sorting
    var latitude: Double? = null,
    var longitude: Double? = null,

    // Dormant V2 Feature: Cloud Synchronization & Cross-Platform compatibility
    var globalSyncId: String = java.util.UUID.randomUUID().toString(),
    var lastModified: Long = System.currentTimeMillis(), // Crucial for resolving sync conflicts

    // Dormant V2 Feature: AI Agent Image Analysis
    var aiVisionTags: String? = null, // e.g., "plumbing, leak, under-sink"
    var aiSummary: String? = null // e.g., "The p-trap pipe appears to have a slow drip."

)
