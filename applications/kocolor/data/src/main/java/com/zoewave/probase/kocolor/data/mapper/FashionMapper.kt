package com.zoewave.probase.kocolor.data.mapper

import com.zoewave.probase.kocolor.db.entity.FashionProfileEntity
import com.zoewave.probase.kocolor.db.entity.SavedSuggestionEntity
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import com.zoewave.probase.core.model.ritual.FashionAdvice

fun FashionProfileEntity.toModel(): FashionProfile = FashionProfile(
    seasonalType = seasonalType,
    undertone = undertone,
    notes = notes,
    recommendedPalette = recommendedPalette
)

fun FashionProfile.toEntity(): FashionProfileEntity = FashionProfileEntity(
    seasonalType = seasonalType,
    undertone = undertone,
    notes = notes,
    recommendedPalette = recommendedPalette
)

fun SavedSuggestionEntity.toModel(): SavedAnalysis = SavedAnalysis(
    id = id,
    timestamp = timestamp,
    advice = advice,
    isSavedToCollection = isSavedToCollection
)

fun SavedAnalysis.toEntity(): SavedSuggestionEntity = SavedSuggestionEntity(
    id = id,
    timestamp = timestamp,
    advice = advice,
    isSavedToCollection = isSavedToCollection
)

fun FashionAdvice.toSavedSuggestionEntity(): SavedSuggestionEntity = SavedSuggestionEntity(
    timestamp = System.currentTimeMillis(),
    advice = this,
    isSavedToCollection = false
)
