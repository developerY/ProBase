package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.core.model.ritual.FashionAdvice

@Entity(tableName = "saved_suggestions")
data class SavedSuggestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val advice: FashionAdvice
)
