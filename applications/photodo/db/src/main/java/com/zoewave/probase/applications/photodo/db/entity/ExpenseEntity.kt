package com.zoewave.probase.applications.photodo.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
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
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0,
    val projectId: Long, // The foreign key linking back to the project
    val description: String,
    val amount: Double
)
