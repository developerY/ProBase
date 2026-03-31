package com.zoewave.probase.applications.photodo.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val categoryId: Long = 0,
    val name: String,
    val description: String? = null,
    val imageUri: String? = null,
    val colorHex: String = "#5C5E7A", // Default brand color
    val isFavorite: Boolean = false,
    val isUrgent: Boolean = false
)
