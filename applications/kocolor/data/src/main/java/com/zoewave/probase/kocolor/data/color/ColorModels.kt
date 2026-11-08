package com.zoewave.probase.kocolor.data.color

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem

data class HSL(val h: Float, val s: Float, val l: Float)

data class LCh(val l: Float, val c: Float, val h: Float)

data class CompositeColorProfile(
    val dominantHues: List<Float> = emptyList(),
    val secondaryHues: List<Float> = emptyList(),
    val temperatureDistribution: Map<String, Float> = emptyMap(),
    val contrastRange: Float = 0f
)

data class CandidateProvenance(
    val clothingItem: ClothingItem? = null,
    val cosmeticItem: CosmeticItem? = null,
    val contextScore: Float = 0f,
    val colorScore: Float = 0f,
    val appearanceScore: Float = 0f,
    val freshnessScore: Float = 0f,
    val compositeScore: Float = 0f,
    val isAnchor: Boolean = false,
    val anchorPriority: Float = 0.0f,
    val retrievalReason: String = ""
) {
    /**
     * True mathematical score normalization: divides raw sub-score sums by theoretical max (5.0f)
     * before rounding with coerceIn(0f, 1f), preventing raw sums (1.5, 2.2, 3.1, 4.0, 4.8) from collapsing to 1.0f.
     */
    val totalScore: Float get() {
        val rawScore = compositeScore.takeIf { it > 0f }
            ?: (contextScore + colorScore + appearanceScore + freshnessScore)
        return (rawScore / 5.0f).coerceIn(0f, 1f)
    }

    val id: String get() = clothingItem?.let { "w_${it.internalId}" } ?: cosmeticItem?.let { "c_${it.internalId}" } ?: "unknown"
    val name: String get() = clothingItem?.name ?: cosmeticItem?.name ?: "Unknown"
}
