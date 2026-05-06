package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone

@Entity(tableName = "fashion_profiles")
data class FashionProfileEntity(
    @PrimaryKey val id: String = "default",
    val seasonalType: SeasonalType,
    val undertone: Undertone,
    val skinToneHex: String?,
    val eyeColor: String?,
    val hairColor: String?,
    val notes: String?,
    val recommendedPalette: List<String>
)
