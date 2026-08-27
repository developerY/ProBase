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
    val retrievalReason: String = ""
) {
    val totalScore: Float get() = compositeScore.takeIf { it > 0f } ?: (contextScore + colorScore + appearanceScore + freshnessScore)
    val id: String get() = clothingItem?.let { "w_${it.internalId}" } ?: cosmeticItem?.let { "c_${it.internalId}" } ?: "unknown"
    val name: String get() = clothingItem?.name ?: cosmeticItem?.name ?: "Unknown"
}
