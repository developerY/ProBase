package com.zoewave.probase.kocolor.fashionista.domain

/**
 * The final reference-calibrated aesthetic evaluation.
 * 
 * @param score The deterministic aesthetic score (0.000 - 100.000).
 * @param coverage The weighted evidence completeness (0.0 - 1.0).
 * @param standardId The identifier for the calibration baseline used.
 * @param standardVersion The version of the calibration standard.
 * @param breakdown The granular feature evidence used to compute the score.
 */
data class FashionistaScore(
    val score: Double,
    val coverage: Double,
    val standardId: String,
    val standardVersion: Int,
    val breakdown: FashionistaFeatureVector
)
