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

data class StyleResult(
    val blueprint: StyleBlueprint,
    val fashionistaScore: FashionistaScore,
    val selectedClothing: List<ClothingItem>,
    val selectedCosmetics: List<CosmeticItem>
)

@Singleton
class GenerateStyleResultUseCase @Inject constructor(
    private val simulatorEngine: StyleSimulatorEngine,
    private val fashionistaEvaluator: FashionistaEvaluator,
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository
) {

    suspend fun execute(intent: String = "Daily Outfit"): StyleResult = withContext(Dispatchers.Default) {
        val wardrobe = wardrobeRepository.getAllClothing().first()
        val cosmetics = cosmeticRepository.getAllCosmetics().first()
        val context = StyleRequestContext(intent = intent)

        val blueprint = simulatorEngine.generateBlueprint(wardrobe, cosmetics, context)
        val fashionistaScore = fashionistaEvaluator.evaluate(blueprint, context)

        val selectedClothing = wardrobe.filter { item ->
            "w_${item.internalId}" in blueprint.selectedClothingIds || item.remoteId in blueprint.selectedClothingIds
        }
        val selectedCosmetics = cosmetics.filter { item ->
            "c_${item.internalId}" in blueprint.selectedCosmeticIds || item.remoteId in blueprint.selectedCosmeticIds
        }

        StyleResult(
            blueprint = blueprint,
            fashionistaScore = fashionistaScore,
            selectedClothing = selectedClothing,
            selectedCosmetics = selectedCosmetics
        )
    }
}
