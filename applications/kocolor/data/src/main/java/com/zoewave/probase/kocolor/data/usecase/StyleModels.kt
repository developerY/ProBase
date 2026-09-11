package com.zoewave.probase.kocolor.data.usecase

import android.graphics.Bitmap
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.color.CompositeColorProfile
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScore
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
data class StyleIntentProfile(
    val colorfulness: Float = 0.5f, 
    val colorContrast: Float = 0.5f,
    val novelty: Float = 0.5f,
    val formality: Float = 0.5f
)

sealed interface StyleIntentState {
    @Serializable
    data object NotSpecified : StyleIntentState

    @Serializable
    data class Specified(val profile: StyleIntentProfile) : StyleIntentState
}

@Serializable
data class StyleRequestContext(
    val requestId: String = UUID.randomUUID().toString(),
    val intent: String,
    val occasion: String = "Daily",
    val weather: String = "Clear",
    val weatherTempC: Float? = null,
    val uvIndex: Float? = null,
    val appearanceTelemetry: ColorTelemetry = ColorTelemetry(),
    val appearanceProfile: AppearanceProfile = AppearanceProfile(),
    val intentProfile: StyleIntentProfile = StyleIntentProfile(),
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

data class RecommendationComposition(
    val clothingSlots: Set<OutfitSlot>,
    val cosmeticRoles: Set<CosmeticRole>,
    val mandatoryAnchors: Set<String>
)

data class ObservedEnsembleMetrics(
    val colorfulness: Float,
    val colorContrast: Float,
    val novelty: Float,
    val formality: Float
)

data class IntentFulfillment(
    val state: StyleIntentState = StyleIntentState.NotSpecified,
    val score: Float? = null,
    val observedMetrics: ObservedEnsembleMetrics = ObservedEnsembleMetrics(0.5f, 0.5f, 0.5f, 0.5f),
    val unmetIntent: List<String> = emptyList()
) {
    val isSpecified: Boolean get() = state is StyleIntentState.Specified
}

enum class CreationPhase {
    IDLE,
    CONTEXT_PARSED,
    ANCHOR_ESTABLISHED,
    AI_GENERATING,
    COMPLETE,
    ERROR
}

data class StyleContextSummary(
    val occasion: String = "Daily Outfit",
    val userIntent: String? = null,
    val appearanceTemperature: String = "Neutral",
    val appearanceDepth: String = "Medium",
    val appearanceContrast: String = "Balanced",
    val temperatureC: Float? = 22.0f,
    val uvIndex: Float? = 3.0f,
    val circadianContext: String = "Defense & Protection",
    
    // Explicit Analytics -> Recommendation Engine Data Flow
    val wardrobeDna: String? = null, 
    val rotationHealth: Int? = null,
    val wardrobeVersatility: Int? = null
)

data class AnchorDecision(
    val anchorName: String = "Universal Khaki Button-Down",
    val anchorId: String = "w_41",
    val anchorReason: String = "Automatic context anchor"
)

data class CandidateSummary(
    val eligibleWardrobeCount: Int = 53,
    val eligibleCosmeticsCount: Int = 25
)

data class ValidationItemModel(
    val label: String,
    val passed: Boolean
)

data class RecommendationValidationResult(
    val isValid: Boolean = true,
    val validationItems: List<ValidationItemModel> = emptyList()
)

data class StyleCreationResult(
    val context: StyleContextSummary,
    val anchor: AnchorDecision,
    val candidateSummary: CandidateSummary,
    val blueprint: StyleBlueprint,
    val validation: RecommendationValidationResult,
    val fashionista: FashionistaScore,
    val intent: IntentFulfillment,
    val selectedClothing: List<ClothingItem>,
    val selectedCosmetics: List<CosmeticItem>,
    val executionTier: String = "AI_CLOUD",
    val latencyMs: Long = 1290L
)
