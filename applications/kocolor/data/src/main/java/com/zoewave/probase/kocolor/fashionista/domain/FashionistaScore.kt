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
    val standardVersion: String = "v1.1",
    val breakdown: FashionistaFeatureVector = FashionistaFeatureVector(
        composition = FeatureValue(0.85, 1.0),
        colorHarmony = FeatureValue(0.85, 1.0),
        silhouette = FeatureValue(0.85, 1.0),
        textureHarmony = FeatureValue(0.85, 1.0),
        visualHierarchy = FeatureValue(0.85, 1.0),
        presentationIntegration = FeatureValue(0.85, 1.0)
    )
)
