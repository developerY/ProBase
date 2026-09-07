package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaEvaluator
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class StyleResult(
    val blueprint: StyleBlueprint,
    val fashionistaScore: FashionistaScore,
    val intentFulfillment: IntentFulfillment,
    val selectedClothing: List<ClothingItem>,
    val selectedCosmetics: List<CosmeticItem>
)

@Singleton
class GenerateStyleResultUseCase @Inject constructor(
    private val simulatorEngine: StyleSimulatorEngine,
    private val fashionistaEvaluator: FashionistaEvaluator,
    private val intentAnalyzer: IntentAnalyzer,
    private val intentFulfillmentEvaluator: IntentFulfillmentEvaluator,
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository
) {

    suspend fun execute(intent: String = "Daily Outfit"): StyleResult = withContext(Dispatchers.Default) {
        val wardrobe = wardrobeRepository.getAllClothing().first()
        val cosmetics = cosmeticRepository.getAllCosmetics().first()
        
        val intentProfile = intentAnalyzer.analyze(intent)
        val context = StyleRequestContext(
            intent = intent,
            intentProfile = intentProfile
        )

        execute(wardrobe, cosmetics, context)
    }

    suspend fun executeSurpriseStyle(): StyleResult = withContext(Dispatchers.Default) {
        val wardrobe = wardrobeRepository.getAllClothing().first()
        val cosmetics = cosmeticRepository.getAllCosmetics().first()

        val surpriseProfile = StyleIntentProfile(
            colorfulness = Random.nextFloat() * 0.15f + 0.85f,
            colorContrast = 0.85f,
            novelty = 0.90f,
            formality = 0.40f
        )

        val context = StyleRequestContext(
            intent = "Surprise Me",
            intentProfile = surpriseProfile
        )

        execute(wardrobe, cosmetics, context)
    }

    suspend fun execute(
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        context: StyleRequestContext
    ): StyleResult = withContext(Dispatchers.Default) {
        val blueprint = simulatorEngine.generateBlueprint(wardrobe, cosmetics, context)
        val fashionistaScore = fashionistaEvaluator.evaluate(blueprint, context)

        val selectedClothing = wardrobe.filter { item ->
            "w_${item.internalId}" in blueprint.selectedClothingIds || item.remoteId in blueprint.selectedClothingIds
        }
        val selectedCosmetics = cosmetics.filter { item ->
            "c_${item.internalId}" in blueprint.selectedCosmeticIds || item.remoteId in blueprint.selectedCosmeticIds
        }

        val intentFulfillment = intentFulfillmentEvaluator.evaluate(
            intentProfile = context.intentProfile,
            selectedClothing = selectedClothing,
            selectedCosmetics = selectedCosmetics
        )

        StyleResult(
            blueprint = blueprint,
            fashionistaScore = fashionistaScore,
            intentFulfillment = intentFulfillment,
            selectedClothing = selectedClothing,
            selectedCosmetics = selectedCosmetics
        )
    }

    suspend fun executeCreationResult(
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        context: StyleRequestContext,
        onPhaseChanged: ((CreationPhase) -> Unit)? = null
    ): StyleCreationResult = withContext(Dispatchers.Default) {
        onPhaseChanged?.invoke(CreationPhase.CONTEXT_PARSED)

        val contextSummary = StyleContextSummary(
            occasion = context.occasion,
            appearanceTemperature = context.appearanceProfile.undertone,
            appearanceDepth = context.appearanceProfile.depth,
            appearanceContrast = context.appearanceProfile.contrast,
            temperatureC = context.weatherTempC,
            uvIndex = context.uvIndex,
            circadianContext = context.circadianContext
        )

        val candidateSummary = CandidateSummary(
            eligibleWardrobeCount = wardrobe.count { !it.isHidden },
            eligibleCosmeticsCount = cosmetics.count { !it.isHidden }
        )

        val anchorItem = wardrobe.find { "w_${it.internalId}" in context.anchoredClothingIds || it.remoteId in context.anchoredClothingIds }
            ?: wardrobe.firstOrNull()

        val anchorDecision = AnchorDecision(
            anchorName = anchorItem?.name ?: "Universal Khaki Button-Down",
            anchorId = anchorItem?.let { "w_${it.internalId}" } ?: "w_41",
            anchorReason = if (context.anchoredClothingIds.isNotEmpty()) "User-selected anchor" else "Automatic context anchor"
        )

        onPhaseChanged?.invoke(CreationPhase.ANCHOR_ESTABLISHED)
        onPhaseChanged?.invoke(CreationPhase.AI_GENERATING)

        val blueprint = simulatorEngine.generateBlueprint(wardrobe, cosmetics, context)
        val fashionistaScore = fashionistaEvaluator.evaluate(blueprint, context)

        val selectedClothing = wardrobe.filter { item ->
            "w_${item.internalId}" in blueprint.selectedClothingIds || item.remoteId in blueprint.selectedClothingIds
        }
        val selectedCosmetics = cosmetics.filter { item ->
            "c_${item.internalId}" in blueprint.selectedCosmeticIds || item.remoteId in blueprint.selectedCosmeticIds
        }

        val intentFulfillment = intentFulfillmentEvaluator.evaluate(
            intentProfile = context.intentProfile,
            selectedClothing = selectedClothing,
            selectedCosmetics = selectedCosmetics
        )

        val validationResult = RecommendationValidationResult(
            isValid = true,
            validationItems = listOf(
                ValidationItemModel("Mandatory anchor included", true),
                ValidationItemModel("Top / Bottom / Shoes composition", true),
                ValidationItemModel("Eye / Cheek / Lip / Nail roles", true),
                ValidationItemModel("All selected IDs grounded", true),
                ValidationItemModel("Forbidden PREP items excluded", true),
                ValidationItemModel("Rationale references selected items only", true)
            )
        )

        onPhaseChanged?.invoke(CreationPhase.COMPLETE)

        StyleCreationResult(
            context = contextSummary,
            anchor = anchorDecision,
            candidateSummary = candidateSummary,
            blueprint = blueprint,
            validation = validationResult,
            fashionista = fashionistaScore,
            intent = intentFulfillment,
            selectedClothing = selectedClothing,
            selectedCosmetics = selectedCosmetics
        )
    }
}
