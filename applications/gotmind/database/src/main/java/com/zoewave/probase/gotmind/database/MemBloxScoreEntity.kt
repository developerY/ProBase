package com.zoewave.probase.gotmind.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "memblox_scores")
data class MemBloxScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)
