package com.zoewave.probase.goswift.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zoewave.probase.goswift.model.CaffeineShot

@Entity(tableName = "shots")
data class ShotEntity(
    @PrimaryKey val id: String,
    val mg: Int,
    val timestamp: Long
)

fun ShotEntity.asExternalModel() = CaffeineShot(
    id = id,
    mg = mg,
    timestamp = timestamp
)

fun CaffeineShot.asEntity() = ShotEntity(
    id = id,
    mg = mg,
    timestamp = timestamp
)
