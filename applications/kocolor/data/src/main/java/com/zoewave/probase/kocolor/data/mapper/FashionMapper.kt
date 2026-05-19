package com.zoewave.probase.kocolor.data.mapper

import com.zoewave.probase.kocolor.db.entity.FashionProfileEntity
import com.zoewave.probase.kocolor.db.entity.SavedSuggestionEntity
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.FashionProfile
import com.zoewave.probase.kocolor.model.SavedAnalysis

fun FashionProfileEntity.toModel(): FashionProfile = FashionProfile(
    id = id,
    seasonalType = seasonalType,
    undertone = undertone,
    skinToneHex = skinToneHex,
    eyeColor = eyeColor,
    hairColor = hairColor,
    notes = notes,
    recommendedPalette = recommendedPalette
)

fun FashionProfile.toEntity(): FashionProfileEntity = FashionProfileEntity(
    id = id,
    seasonalType = seasonalType,
    undertone = undertone,
    skinToneHex = skinToneHex,
    eyeColor = eyeColor,
    hairColor = hairColor,
    notes = notes,
    recommendedPalette = recommendedPalette
)

fun SavedSuggestionEntity.toModel(): SavedAnalysis = SavedAnalysis(
    id = id,
    timestamp = timestamp,
    advice = advice
)

fun SavedAnalysis.toEntity(): SavedSuggestionEntity = SavedSuggestionEntity(
    id = id,
    timestamp = timestamp,
    advice = advice
)

fun FashionAdvice.toSavedSuggestionEntity(): SavedSuggestionEntity = SavedSuggestionEntity(
    timestamp = System.currentTimeMillis(),
    advice = this
)
