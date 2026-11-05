package com.zoewave.probase.kocolor.fashionista.domain

/**
 * Immutable evaluation snapshot emitted by the FASHIONISTA Engine.
 */
data class FashionistaScore(
    val colorHarmonyScore: Float = 0f,
    val silhouetteScore: Float = 0f,
    val contrastScore: Float = 0f,
    val totalScore: Float = 0f,
    val isApproved: Boolean = totalScore >= 80.0f,
    val score: Double = totalScore.toDouble(),
    val coverage: Double = 1.0,
    val standardId: String = "FASHIONISTA_STD",
    val standardVersion: String = "v1",
    val breakdown: FashionistaFeatureVector = FashionistaFeatureVector(
        colorHarmony = FeatureValue(colorHarmonyScore.toDouble() / 100.0, 1.0),
        silhouette = FeatureValue(silhouetteScore.toDouble() / 100.0, 1.0),
        contrast = FeatureValue(contrastScore.toDouble() / 100.0, 1.0)
    )
)

data class FashionistaFeatureVector(
    val colorHarmony: FeatureValue,
    val silhouette: FeatureValue,
    val contrast: FeatureValue
)

data class FeatureValue(
    val value: Double,
    val availability: Double
)
