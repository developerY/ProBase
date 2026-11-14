package com.zoewave.probase.kocolor.fashionista.scoring

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.util.KeywordMatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SilhouetteGrader @Inject constructor() {

    fun grade(blueprint: StyleBlueprint, resolvedClothing: List<ClothingItem> = emptyList()): Float {
        val selectedItems = resolvedClothing
        if (selectedItems.isEmpty()) return 0f

        val top = selectedItems.find { it.category == ClothingCategory.TOPS || it.category == ClothingCategory.ACTIVEWEAR || it.category == ClothingCategory.DRESSES }
        val bottom = selectedItems.find { it.category == ClothingCategory.BOTTOMS }

        var scoreModifier = 0f

        if (top != null && top.category == ClothingCategory.DRESSES) {
            val dressTags = extractSilhouetteTags(top)
            scoreModifier += 25.0f // Baseline structural cohesion for one-piece
            
            if (dressTags.contains("CUT")) {
                scoreModifier += 10.0f
            }
            if (selectedItems.any { it.category == ClothingCategory.SHOES }) {
                scoreModifier += 10.0f
            }
        } else if (top != null && bottom != null) {
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
                // Box Penalty: Top Volume paired with Bottom Volume (swallows frame)
                isTopVolume && isBottomVolume && !isTopCropped -> {
                    scoreModifier -= 35.0f
                }
                // Tapered Harmony: Top Volume + Fitted Bottom Cut, OR Fitted Top Cut + Bottom Volume
                (isTopVolume && isBottomCut) || (isTopCut && isBottomVolume) || (isTopCropped && isBottomVolume) -> {
                    scoreModifier += 32.0f
                }
                isTopCut && isBottomCut -> {
                    scoreModifier += 22.0f
                }
            }

            // 2. Evaluate Proportion Alignment (Rule of Thirds)
            if (isTopCropped && isBottomHighWaisted) {
                scoreModifier += 18.0f
            }

            if (selectedItems.any { it.category == ClothingCategory.SHOES }) {
                scoreModifier += 10.0f // Complete 3-piece credit
            }
        }

        return (50.0f + scoreModifier).coerceIn(0.0f, 100.0f)
    }

    private fun extractSilhouetteTags(item: ClothingItem): Set<String> {
        val text = "${item.name} ${item.notes ?: ""} ${item.material ?: ""}".lowercase()
        val tags = mutableSetOf<String>()

        // Top Volume
        if (KeywordMatcher.containsWholeWord(text, "oversized") || KeywordMatcher.containsWholeWord(text, "puffer") || KeywordMatcher.containsWholeWord(text, "chunky") || KeywordMatcher.containsWholeWord(text, "relaxed") || KeywordMatcher.containsWholeWord(text, "boxy") || KeywordMatcher.containsWholeWord(text, "hoodie") || KeywordMatcher.containsWholeWord(text, "loose")) {
            tags.add("VOLUME")
        }
        // Top Cut
        if (KeywordMatcher.containsWholeWord(text, "cropped") || KeywordMatcher.containsWholeWord(text, "fitted") || KeywordMatcher.containsWholeWord(text, "bodysuit") || KeywordMatcher.containsWholeWord(text, "slim") || KeywordMatcher.containsWholeWord(text, "tailored") || KeywordMatcher.containsWholeWord(text, "ribbed") || KeywordMatcher.containsWholeWord(text, "blouse") || KeywordMatcher.containsWholeWord(text, "camisole") || KeywordMatcher.containsWholeWord(text, "turtleneck") || KeywordMatcher.containsWholeWord(text, "oxford") || KeywordMatcher.containsWholeWord(text, "structured")) {
            tags.add("CUT")
        }
        if (KeywordMatcher.containsWholeWord(text, "cropped") || KeywordMatcher.containsWholeWord(text, "crop")) {
            tags.add("CROPPED")
        }

        // Bottom Volume
        if (KeywordMatcher.containsWholeWord(text, "wide leg") || KeywordMatcher.containsWholeWord(text, "wide") || KeywordMatcher.containsWholeWord(text, "cargo") || KeywordMatcher.containsWholeWord(text, "culottes") || KeywordMatcher.containsWholeWord(text, "culotte") || KeywordMatcher.containsWholeWord(text, "pleated") || KeywordMatcher.containsWholeWord(text, "flared") || KeywordMatcher.containsWholeWord(text, "relaxed") || KeywordMatcher.containsWholeWord(text, "slacks") || KeywordMatcher.containsWholeWord(text, "baggy")) {
            tags.add("VOLUME")
        }
        // Bottom Cut
        if (KeywordMatcher.containsWholeWord(text, "fitted") || KeywordMatcher.containsWholeWord(text, "leggings") || KeywordMatcher.containsWholeWord(text, "legging") || KeywordMatcher.containsWholeWord(text, "slim") || KeywordMatcher.containsWholeWord(text, "tailored") || KeywordMatcher.containsWholeWord(text, "straight") || KeywordMatcher.containsWholeWord(text, "denim") || KeywordMatcher.containsWholeWord(text, "jeans") || KeywordMatcher.containsWholeWord(text, "leather") || KeywordMatcher.containsWholeWord(text, "skinny")) {
            tags.add("CUT")
        }
        if (KeywordMatcher.containsWholeWord(text, "high waisted") || KeywordMatcher.containsWholeWord(text, "high") || KeywordMatcher.containsWholeWord(text, "culottes") || KeywordMatcher.containsWholeWord(text, "pleated") || KeywordMatcher.containsWholeWord(text, "flared")) {
            tags.add("HIGH_WAISTED")
        }

        return tags
    }
}
