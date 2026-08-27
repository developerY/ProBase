package com.zoewave.probase.kocolor.data.usecase

import android.graphics.Bitmap
import com.zoewave.probase.features.ai.firebase.models.Appearance
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.UUID

@Serializable
data class StyleRequestContext(
    val requestId: String = UUID.randomUUID().toString(),
    val intent: String,
    val occasion: String = "Daily",
    val weather: String,
    val appearanceTelemetry: Appearance,
    val circadianContext: String = "Defense & Protection",
    val wellnessScore: Double = 0.85,
    val anchoredClothingIds: List<String> = emptyList(),
    val anchoredCosmeticIds: List<String> = emptyList(),
    val rotationScores: Map<String, Double> = emptyMap(),
    val fashionProfile: String? = null,
    val userLockedAnchorId: String? = null,
    val userSelectedAnchorId: String? = null,
    @Transient val localImageBitmap: Bitmap? = null
)

@Serializable
data class StyleBlueprint(
    val rationale: String,
    val selectedClothingIds: List<String>,
    val selectedCosmeticIds: List<String>,
    val recommendedPalette: List<String>
)
