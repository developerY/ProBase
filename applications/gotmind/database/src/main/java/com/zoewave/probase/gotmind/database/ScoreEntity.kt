package com.zoewave.probase.gotmind.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val value: Int,
    val timestamp: Long
)
