package com.zoewave.probase.kocolor.data.usecase

import android.graphics.Bitmap
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class StyleRequestContext(
    val intent: String,
    val weather: String,
    val appearanceTelemetry: String,
    val circadianContext: String = "Defense & Protection",
    val wellnessScore: Double = 0.85,
    val anchoredClothingIds: List<String> = emptyList(),
    val anchoredCosmeticIds: List<String> = emptyList(),
    val rotationScores: Map<String, Double> = emptyMap(),
    val fashionProfile: String? = null,
    @Transient val localImageBitmap: Bitmap? = null
)

@Serializable
data class StyleBlueprint(
    val rationale: String,
    val selectedClothingIds: List<String>,
    val selectedCosmeticIds: List<String>,
    val recommendedPalette: List<String>
)
