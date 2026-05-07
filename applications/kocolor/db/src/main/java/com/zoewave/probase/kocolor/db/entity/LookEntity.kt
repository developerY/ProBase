package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "looks")
data class LookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val faceItemId: Long?,
    val hairItemId: Long?,
    val clothesItemId: Long?,
    val shoesItemId: Long?,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null
)
