package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeCandidateFilter @Inject constructor(
    private val repository: WardrobeRepository
) {

    /**
     * Local Candidate RAG Engine (The 85/15 Local-First split).
     * Reduces the full wardrobe inventory to an initial ranked candidate pool.
     */
    suspend fun getCandidates(
        inventory: List<ClothingItem>,
        context: StyleRequestContext,
        limit: Int
    ): List<ClothingItem> {
        val eligibleItems = inventory.filter { item ->
            isAvailable(item) && 
            isWeatherCompatible(item, context.weather) && 
            !isRotationViolated(item, context.rotationScores)
        }

        // Priority for anchored items: if they are in the eligible list, they should be at the top
        val anchoredItems = eligibleItems.filter { "w_${it.internalId}" in context.anchoredClothingIds }
        val remainingItems = eligibleItems.filter { "w_${it.internalId}" !in context.anchoredClothingIds }

        // Stage 2: Soft Scoring & Ranking
        val rankedRemaining = remainingItems.map { item ->
            item to calculateScore(item, context)
        }.sortedByDescending { it.second }
         .map { it.first }

        // Stage 3: Pool Truncation
        val combined = (anchoredItems + rankedRemaining).distinctBy { it.internalId }
        return combined.take(limit)
    }

    private fun isAvailable(item: ClothingItem): Boolean {
        // Assuming a field or logic for availability
        return true 
    }

    private fun isWeatherCompatible(item: ClothingItem, weather: String): Boolean {
        // Basic heuristic: check if weather context mentions temp and compare with thermal weight
        return true
    }

    private fun isRotationViolated(item: ClothingItem, scores: Map<String, Double>): Boolean {
        // Use the passed rotation scores (penalty >= 0.70 means skip)
        val penalty = scores[item.remoteId] ?: 0.0
        return penalty >= 0.70
    }

    private fun calculateScore(item: ClothingItem, context: StyleRequestContext): Double {
        var score = 0.0
        
        // 1. Match against AppearanceTelemetry (undertone match, contrast delta)
        if (context.appearanceTelemetry.contains(item.colorTemperature ?: "", ignoreCase = true)) {
            score += 10.0
        }
        
        // 2. Score contextual relevance against userIntent keywords and occasion tags
        val keywords = context.intent.lowercase().split(" ", ",", ".")
        if (keywords.any { item.name.contains(it, ignoreCase = true) || (item.notes?.contains(it, ignoreCase = true) ?: false) }) {
            score += 20.0
        }
        
        return score
    }
}
