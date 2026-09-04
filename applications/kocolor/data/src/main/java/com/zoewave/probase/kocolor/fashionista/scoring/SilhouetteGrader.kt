package com.zoewave.probase.kocolor.fashionista.scoring

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SilhouetteGrader @Inject constructor(
    private val wardrobeRepository: WardrobeRepository
) {

    fun grade(blueprint: StyleBlueprint, inventory: List<ClothingItem> = emptyList()): Float {
        val clothingIds = blueprint.selectedClothingIds
        if (clothingIds.isEmpty()) return 75.0f

        val fullInventory = if (inventory.isNotEmpty()) {
            inventory
        } else {
            try {
                runBlocking { wardrobeRepository.getAllClothing().first() }
            } catch (e: Exception) {
                emptyList()
            }
        }

        val selectedItems = clothingIds.mapNotNull { id ->
            val cleanId = id.removePrefix("w_").toLongOrNull()
            fullInventory.find { it.internalId == cleanId || "w_${it.internalId}" == id || it.remoteId == id }
        }

        if (selectedItems.isEmpty()) return 75.0f

        val top = selectedItems.find { it.category == ClothingCategory.TOPS || it.category == ClothingCategory.DRESSES }
        val bottom = selectedItems.find { it.category == ClothingCategory.BOTTOMS }

        var scoreModifier = 0f

        if (top != null && bottom != null) {
            val topTags = extractSilhouetteTags(top)
            val bottomTags = extractSilhouetteTags(bottom)

            val isTopVolume = topTags.contains("VOLUME")
            val isTopCut = topTags.contains("CUT")
            val isTopCropped = topTags.contains("CROPPED")

            val isBottomVolume = bottomTags.contains("VOLUME")
            val isBottomCut = bottomTags.contains("CUT")
            val isBottomHighWaisted = bottomTags.contains("HIGH_WAISTED")

            // 1. Evaluate Volume Balance
            when {
                // Box Penalty (-20.0f): Top Volume paired with Bottom Volume (swallows frame)
                isTopVolume && isBottomVolume -> {
                    scoreModifier -= 20.0f
                }
                // Tapered Harmony (+15.0f): Top Volume + Fitted Bottom Cut, OR Fitted Top Cut + Bottom Volume
                (isTopVolume && isBottomCut) || (isTopCut && isBottomVolume) -> {
                    scoreModifier += 15.0f
                }
            }

            // 2. Evaluate Proportion Alignment (Rule of Thirds) (+15.0f)
            if (isTopCropped && isBottomHighWaisted) {
                scoreModifier += 15.0f
            }
        } else if (selectedItems.size >= 3) {
            scoreModifier += 10.0f // Complete 3-piece credit
        }

        return (75.0f + scoreModifier).coerceIn(0.0f, 100.0f)
    }

    private fun extractSilhouetteTags(item: ClothingItem): Set<String> {
        val text = "${item.name} ${item.notes ?: ""} ${item.material ?: ""}".lowercase()
        val tags = mutableSetOf<String>()

        // Top Volume
        if (text.contains("oversized") || text.contains("puffer") || text.contains("chunky") || text.contains("relaxed") || text.contains("boxy")) {
            tags.add("VOLUME")
        }
        // Top Cut
        if (text.contains("cropped") || text.contains("fitted") || text.contains("bodysuit") || text.contains("slim")) {
            tags.add("CUT")
        }
        if (text.contains("cropped")) {
            tags.add("CROPPED")
        }

        // Bottom Volume
        if (text.contains("wide-leg") || text.contains("wide leg") || text.contains("cargo") || text.contains("culottes") || text.contains("flared") || text.contains("baggy")) {
            tags.add("VOLUME")
        }
        // Bottom Cut
        if (text.contains("fitted") || text.contains("legging") || text.contains("high-waisted") || text.contains("high waisted") || text.contains("skinny")) {
            tags.add("CUT")
        }
        if (text.contains("high-waisted") || text.contains("high waisted")) {
            tags.add("HIGH_WAISTED")
        }

        return tags
    }
}
