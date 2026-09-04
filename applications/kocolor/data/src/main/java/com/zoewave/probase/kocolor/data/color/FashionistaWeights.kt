package com.zoewave.probase.kocolor.data.color

/**
 * Centralized FASHIONISTA scoring constants for candidate filtering,
 * thermal alignment, color clash penalties, and seasonal harmony.
 */
object FashionistaWeights {
    const val SEASONAL_HARMONY_BONUS = 2.0f
    const val WEATHER_ALIGNMENT_BONUS = 1.5f
    const val NEUTRAL_ANCHOR_BONUS = 1.25f
    const val COLOR_CLASH_PENALTY = -2.5f
    const val THERMAL_MISMATCH_PENALTY = -3.0f
}
