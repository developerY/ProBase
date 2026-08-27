package com.zoewave.probase.kocolor.data.color

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem

data class HSL(val h: Float, val s: Float, val l: Float)

data class CandidateProvenance(
    val clothingItem: ClothingItem? = null,
    val cosmeticItem: CosmeticItem? = null,
    val contextScore: Float,
    val colorScore: Float,
    val appearanceScore: Float,
    val freshnessScore: Float,
    val retrievalReason: String
) {
    val totalScore: Float get() = contextScore + colorScore + appearanceScore + freshnessScore
    val id: String get() = clothingItem?.let { "w_${it.internalId}" } ?: cosmeticItem?.let { "c_${it.internalId}" } ?: "unknown"
    val name: String get() = clothingItem?.name ?: cosmeticItem?.name ?: "Unknown"
}
