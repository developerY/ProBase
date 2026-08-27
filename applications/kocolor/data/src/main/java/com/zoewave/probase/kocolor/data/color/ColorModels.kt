package com.zoewave.probase.kocolor.data.color

import com.zoewave.probase.core.model.ritual.ClothingItem

data class HSL(val h: Float, val s: Float, val l: Float)

data class CandidateProvenance(
    val item: ClothingItem,
    val contextScore: Float,
    val colorScore: Float,
    val appearanceScore: Float,
    val freshnessScore: Float,
    val retrievalReason: String
) {
    val totalScore: Float get() = contextScore + colorScore + appearanceScore + freshnessScore
}
