package com.zoewave.probase.kocolor.db.entity

import kotlinx.serialization.Serializable

/**
 * Deterministic generation provenance. Immutable after creation.
 */
data class SelectionEvidence(
    val compatibilityScore: Double = 0.0,
    val rotationPenalty: Double = 0.0,
    val weatherScore: Double = 0.0,
    val contextScore: Double = 0.0,
    val colorScore: Double = 0.0,
    val cosmeticScore: Double = 0.0,
    val combinedFinalScore: Double = 0.0,
    val scoringVersion: String = "v1.0"
)

data class SelectionRationale(
    val calendarReason: String? = null,
    val weatherReason: String? = null,
    val locationReason: String? = null,
    val colorReason: String? = null,
    val rotationReason: String? = null,
    val cosmeticReason: String? = null
)
