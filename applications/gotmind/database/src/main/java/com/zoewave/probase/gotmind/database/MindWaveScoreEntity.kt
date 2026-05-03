package com.zoewave.probase.gotmind.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.util.UUID

@Entity(tableName = "mindwave_scores")
data class MindWaveScoreEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val score: Int,
    val level: Int,
    val timestamp: Long = System.currentTimeMillis()
)
