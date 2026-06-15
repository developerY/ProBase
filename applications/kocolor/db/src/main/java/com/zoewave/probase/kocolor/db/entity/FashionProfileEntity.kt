package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone

@Entity(tableName = "fashion_profiles")
data class FashionProfileEntity(
    @PrimaryKey val id: String = "default",
    val seasonalType: SeasonalType,
    val undertone: Undertone,
    val skinToneHex: String? = null,
    val eyeColor: String? = null,
    val hairColor: String? = null,
    val notes: String? = null,
    val recommendedPalette: List<String> = emptyList()
)
