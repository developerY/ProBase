package com.zoewave.probase.applications.photodo.db.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
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
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["creationDate"])
    ]
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
    val projectBudget: Double = 0.0, // The total allowed budget
    val currentSpend: Double = 0.0,   // The running total of expenses
    var priority: Int = 0,
    val creationDate: Long = System.currentTimeMillis(),
    var dueDate: Long? = null,
    var isAlarmEnabled: Boolean = false,
    val isArchived: Boolean = false,
    var globalSyncId: String = UUID.randomUUID().toString(),
    var lastModified: Long = System.currentTimeMillis()
)
