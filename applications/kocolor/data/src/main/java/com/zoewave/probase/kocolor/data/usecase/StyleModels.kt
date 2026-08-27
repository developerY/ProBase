package com.zoewave.probase.kocolor.data.usecase

import android.graphics.Bitmap
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.color.CompositeColorProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.UUID

@Serializable
data class ColorTelemetry(
    val undertoneScore: Float = 0f,
    val depthScore: Float = 0.5f,
    val contrastScore: Float = 0.5f
)

@Serializable
data class AppearanceProfile(
    val undertone: String = "Neutral",
    val depth: String = "Medium",
    val contrast: String = "Balanced"
)

@Serializable
enum class SelectionTier {
    SELECTED,
    LOCKED,
    FORCED
}

@Serializable
data class UserConstraint(
    val itemId: String,
    val category: String,
    val tier: SelectionTier = SelectionTier.LOCKED
)

@Serializable
data class RoleRequirement(
    val role: String,
    val minCount: Int = 1,
    val maxCount: Int? = null
)

@Serializable
data class StyleRequestContext(
    val requestId: String = UUID.randomUUID().toString(),
    val intent: String,
    val occasion: String = "Daily",
    val weather: String = "Clear",
    val weatherTempC: Float = 22f,
    val uvIndex: Float = 3f,
    val appearanceTelemetry: ColorTelemetry = ColorTelemetry(),
    val appearanceProfile: AppearanceProfile = AppearanceProfile(),
    val circadianContext: String = "Defense & Protection",
    val wellnessScore: Double = 0.85,
    val anchoredClothingIds: List<String> = emptyList(),
    val anchoredCosmeticIds: List<String> = emptyList(),
    val rotationScores: Map<String, Double> = emptyMap(),
    val fashionProfile: String? = null,
    val lockedConstraints: List<UserConstraint> = emptyList(),
    @Transient val localImageBitmap: Bitmap? = null
)

data class StyleSelectionState(
    val activeAnchors: List<ClothingItem> = emptyList(),
    val missingRoleRequirements: List<RoleRequirement> = emptyList(),
    val compositeProfile: CompositeColorProfile = CompositeColorProfile(),
    val fullRankedCandidatePool: List<CandidateProvenance> = emptyList()
)

@Serializable
data class StyleBlueprint(
    val rationale: String,
    val selectedClothingIds: List<String>,
    val selectedCosmeticIds: List<String>,
    val recommendedPalette: List<String>
)
