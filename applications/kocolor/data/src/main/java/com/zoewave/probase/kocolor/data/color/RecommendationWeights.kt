package com.zoewave.probase.kocolor.data.color

/**
 * These weights govern deterministic candidate retrieval and pruning.
 * They are NOT normalized FASHIONISTA aesthetic scores.
 */
object RecommendationWeights {
    const val APPEARANCE_TEMPERATURE_HARMONY_BONUS = 2.0f
    const val WEATHER_ALIGNMENT_BONUS = 1.5f
    const val NEUTRAL_ANCHOR_BONUS = 1.25f
    const val HIGH_CHROMA_INTENT_BONUS = 2.5f
    const val APPEARANCE_TEMPERATURE_CLASH_PENALTY = -2.5f
    const val THERMAL_MISMATCH_PENALTY = -3.0f
    const val MONOCHROME_NEUTRAL_PENALTY = -1.5f
}
