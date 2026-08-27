package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeCandidateFilter @Inject constructor() {

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
        // Filter out garments marked as hidden or archived.
        return !item.isHidden
    }

    private fun isWeatherCompatible(item: ClothingItem, weather: String): Boolean {
        // Basic heuristic: check if weather context mentions high temp and filter heavy categories
        val isHot = weather.contains("Temp: 2", ignoreCase = true) || weather.contains("Temp: 3", ignoreCase = true)
        val isCold = weather.contains("Temp: -", ignoreCase = true) || weather.contains("Temp: 0", ignoreCase = true) || weather.contains("Temp: 5", ignoreCase = true)
        
        return when {
            isHot -> item.category != ClothingCategory.OUTERWEAR
            isCold -> item.category != ClothingCategory.ACTIVEWEAR // assuming shorts etc
            else -> true
        }
    }

    private fun isRotationViolated(item: ClothingItem, scores: Map<String, Double>): Boolean {
        // 1. Use the passed rotation scores (calculated by the projected state if applicable)
        val penalty = scores[item.remoteId] ?: 0.0
        if (penalty >= 0.70) return true
        
        // 2. Fallback to physical lastUsedTimestamp if score is missing (Exclude if worn in last 3 days)
        val lastUsed = item.lastUsedTimestamp ?: 0L
        val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
        return lastUsed > threeDaysAgo
    }

    private fun calculateScore(item: ClothingItem, context: StyleRequestContext): Double {
        var score = 0.0
        
        // 1. Match against AppearanceTelemetry (undertone match, contrast delta)
        val appearanceString = "${context.appearanceTelemetry.temperature} • ${context.appearanceTelemetry.depth} • ${context.appearanceTelemetry.contrast}"
        if (appearanceString.contains(item.colorTemperature ?: "", ignoreCase = true)) {
            score += 10.0
        }
        
        // 2. Score contextual relevance against userIntent keywords and occasion tags
        val keywords = context.intent.lowercase().split(" ", ",", ".")
        if (keywords.any { item.name.contains(it, ignoreCase = true) || (item.notes?.contains(it, ignoreCase = true) ?: false) }) {
            score += 20.0
        }
        
        return score
    }

    suspend fun getCosmeticCandidates(
        inventory: List<CosmeticItem>,
        context: StyleRequestContext,
        limit: Int
    ): List<CosmeticItem> {
        val noiseCategories = setOf("oral", "tools", "fragrance", "grooming", "organizers")
        
        val eligibleItems = inventory.filter { item ->
            !item.isHidden && 
            !noiseCategories.contains(item.macroCategory.name.lowercase())
        }

        // Priority for anchored items
        val anchoredItems = eligibleItems.filter { "c_${it.internalId}" in context.anchoredCosmeticIds }
        val remainingItems = eligibleItems.filter { "c_${it.internalId}" !in context.anchoredCosmeticIds }

        // Stage 2: Soft Scoring & Ranking
        val rankedRemaining = remainingItems.map { item ->
            item to calculateCosmeticScore(item, context)
        }.sortedByDescending { it.second }
         .map { it.first }

        // Stage 3: Pool Truncation
        val combined = (anchoredItems + rankedRemaining).distinctBy { it.internalId }
        return combined.take(limit)
    }

    private fun calculateCosmeticScore(item: CosmeticItem, context: StyleRequestContext): Double {
        var score = 0.0
        val appearanceString = "${context.appearanceTelemetry.temperature} • ${context.appearanceTelemetry.depth} • ${context.appearanceTelemetry.contrast}"
        if (appearanceString.contains(item.temperature.name, ignoreCase = true)) {
            score += 10.0
        }
        val keywords = context.intent.lowercase().split(" ", ",", ".")
        if (keywords.any { item.name.contains(it, ignoreCase = true) || (item.notes?.contains(it, ignoreCase = true) ?: false) }) {
            score += 20.0
        }
        return score
    }
}
