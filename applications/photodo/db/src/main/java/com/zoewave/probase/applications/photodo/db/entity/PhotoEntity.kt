package com.zoewave.probase.applications.photodo.db.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "photos",
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
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val photoId: Long = 0,
    val projectId: Long,
    val photoUri: String,
    val caption: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    var latitude: Double? = null,
    var longitude: Double? = null,
    var globalSyncId: String = UUID.randomUUID().toString(),
    var lastModified: Long = System.currentTimeMillis(),
    var aiVisionTags: String? = null,
    var aiSummary: String? = null
)
