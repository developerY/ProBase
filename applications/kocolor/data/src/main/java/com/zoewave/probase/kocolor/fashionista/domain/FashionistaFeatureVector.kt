package com.zoewave.probase.kocolor.fashionista.domain

/**
 * The 6-dimensional perceptual breakdown of the observed outfit.
 */
data class FashionistaFeatureVector(
    val composition: FeatureValue,
    val colorHarmony: FeatureValue,
    val silhouette: FeatureValue,
    val textureHarmony: FeatureValue,
    val visualHierarchy: FeatureValue,
    val presentationIntegration: FeatureValue
)
