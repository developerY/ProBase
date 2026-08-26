package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeuristicStyleEngine @Inject constructor(
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository
) : DeterministicStyleEngine {

    override fun generate(context: StyleRequestContext): StyleBlueprint {
        // Use runBlocking for the deterministic fallback as it's usually called as a last resort
        // and needs to return a non-suspend result based on the interface.
        // In a real app, this should ideally be async or pre-fetched.
        val availableWardrobe = runBlocking { wardrobeRepository.getAllClothing().first() }
        val availableCosmetics = runBlocking { cosmeticRepository.getAllCosmetics().first() }

        val selectedItems = mutableListOf<ClothingItem>()
        val selectedCosmetics = mutableListOf<CosmeticItem>()
        
        // 1. Pick Clothes
        val tops = availableWardrobe.filter { it.category == ClothingCategory.TOPS }
        val bottoms = availableWardrobe.filter { it.category == ClothingCategory.BOTTOMS }
        val shoes = availableWardrobe.filter { it.category == ClothingCategory.SHOES }
        
        fun <T> List<T>.smartPick(nameSelector: (T) -> String, notesSelector: (T) -> String?): T? {
            if (this.isEmpty()) return null
            val matches = this.filter { item ->
                context.intent.split(" ").any { keyword -> 
                    nameSelector(item).contains(keyword, ignoreCase = true) || 
                    (notesSelector(item)?.contains(keyword, ignoreCase = true) ?: false)
                }
            }
            return matches.randomOrNull() ?: this.random()
        }

        tops.smartPick({it.name}, {it.notes})?.let { selectedItems.add(it) }
        bottoms.smartPick({it.name}, {it.notes})?.let { selectedItems.add(it) }
        shoes.smartPick({it.name}, {it.notes})?.let { selectedItems.add(it) }

        // 2. Pick Cosmetics
        val eyes = availableCosmetics.filter { it.macroCategory == MacroCategory.EYES }
        val lips = availableCosmetics.filter { it.macroCategory == MacroCategory.LIPS }

        eyes.smartPick({it.name}, {it.notes})?.let { selectedCosmetics.add(it) }
        lips.smartPick({it.name}, {it.notes})?.let { selectedCosmetics.add(it) }

        val palette = selectedItems.mapNotNull { it.dominantHex }.distinct().toMutableList()
        if (palette.isEmpty()) palette.add("#FFFFFF")
        
        while (palette.size < 4) {
            palette.add(listOf("#000000", "#808080", "#E0E0E0", "#333333").random())
        }

        return StyleBlueprint(
            rationale = "Local Architect: Selected from your vault based on intent and availability.",
            selectedClothingIds = selectedItems.map { "w_${it.internalId}" },
            selectedCosmeticIds = selectedCosmetics.map { "c_${it.internalId}" },
            recommendedPalette = palette.take(4)
        )
    }
}
