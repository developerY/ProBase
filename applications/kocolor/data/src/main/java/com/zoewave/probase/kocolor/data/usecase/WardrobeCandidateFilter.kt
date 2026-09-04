package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeCandidateFilter @Inject constructor(
    private val rotationScoringUseCase: RotationScoringUseCase
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
        
        // 1. Match against AppearanceProfile (undertone match, contrast delta)
        val appearanceString = "${context.appearanceProfile.undertone} • ${context.appearanceProfile.depth} • ${context.appearanceProfile.contrast}"
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

    suspend fun getCosmeticCandidateProvenance(
        inventory: List<CosmeticItem>,
        context: StyleRequestContext,
        limit: Int
    ): List<CandidateProvenance> = withContext(Dispatchers.Default) {
        val noiseCategories = setOf("oral", "tools", "fragrance", "grooming", "organizers")
        
        val eligibleItems = inventory.filter { item ->
            !item.isHidden && 
            !noiseCategories.contains(item.macroCategory.name.lowercase()) &&
            !isCosmeticRotationViolated(item)
        }

        // Priority for anchored items
        val anchoredItems = eligibleItems.filter { "c_${it.internalId}" in context.anchoredCosmeticIds }
        val remainingItems = eligibleItems.filter { "c_${it.internalId}" !in context.anchoredCosmeticIds }

        // Stage 2: Soft Scoring & Ranking with CandidateProvenance
        val rankedRemainingProv = remainingItems.map { item ->
            val score = calculateCosmeticScore(item, context)
            CandidateProvenance(
                cosmeticItem = item,
                contextScore = score.toFloat(),
                colorScore = 0.8f,
                appearanceScore = 0.8f,
                freshnessScore = 1.0f,
                retrievalReason = "Relational temperature match (${item.temperature.name})"
            )
        }.sortedByDescending { it.totalScore }

        val anchoredProv = anchoredItems.map { item ->
            CandidateProvenance(
                cosmeticItem = item,
                contextScore = 2.0f,
                colorScore = 1.0f,
                appearanceScore = 1.0f,
                freshnessScore = 1.0f,
                retrievalReason = if (item.isSignature) "[Signature Item] Rotation bypassed." else "[LOCKED ANCHOR] Required cosmetic anchor"
            )
        }

        val targetCategories = setOf(
            MacroCategory.EYES,
            MacroCategory.DIMENSION,
            MacroCategory.LIPS,
            MacroCategory.NAILS
        )

        val diverseSet = mutableListOf<CandidateProvenance>()
        diverseSet.addAll(anchoredProv)

        for (category in targetCategories) {
            val categoryMatches = rankedRemainingProv.filter { it.cosmeticItem?.macroCategory == category }
            diverseSet.addAll(categoryMatches.take(2))
        }

        for (prov in rankedRemainingProv) {
            if (diverseSet.size >= limit) break
            if (prov !in diverseSet) {
                diverseSet.add(prov)
            }
        }

        diverseSet.take(limit)
    }

    suspend fun getCosmeticCandidates(
        inventory: List<CosmeticItem>,
        context: StyleRequestContext,
        limit: Int
    ): List<CosmeticItem> {
        return getCosmeticCandidateProvenance(inventory, context, limit).mapNotNull { it.cosmeticItem }
    }

    private fun calculateCosmeticScore(item: CosmeticItem, context: StyleRequestContext): Double {
        var score = 1.0 // Base score
        val appearance = context.appearanceProfile
        val telemetry = context.appearanceTelemetry

        // 1. Relational Temperature Boost (evaluates raw float threshold + string label)
        val isWarmContext = telemetry.undertoneScore > 0.02f ||
                appearance.undertone.contains("Warm", ignoreCase = true) ||
                appearance.undertone.contains("Golden", ignoreCase = true) ||
                appearance.undertone.contains("Peach", ignoreCase = true)

        val isCoolContext = telemetry.undertoneScore < -0.02f ||
                appearance.undertone.contains("Cool", ignoreCase = true) ||
                appearance.undertone.contains("Pink", ignoreCase = true) ||
                appearance.undertone.contains("Blue", ignoreCase = true)

        val cosmeticTemp = item.temperature.name.uppercase()
        when {
            isWarmContext && (cosmeticTemp.contains("WARM") || cosmeticTemp.contains("GOLDEN")) -> score += 1.85
            isCoolContext && (cosmeticTemp.contains("COOL") || cosmeticTemp.contains("ROSY")) -> score += 1.85
            cosmeticTemp.contains("NEUTRAL") -> score += 1.25
            else -> score += 0.60
        }

        // 2. Keyword & Intent Relevance
        val keywords = context.intent.lowercase().split(" ", ",", ".")
        if (keywords.any { item.name.contains(it, ignoreCase = true) || (item.notes?.contains(it, ignoreCase = true) ?: false) }) {
            score += 0.75
        }

        // 3. Signature Item Boost
        if (item.isSignature) {
            score += 0.25
        }

        return score
    }

    private suspend fun isCosmeticRotationViolated(item: CosmeticItem): Boolean {
        val penalty = rotationScoringUseCase.calculateRotationPenalty(
            productId = item.remoteId ?: "c_${item.internalId}",
            category = item.macroCategory.name,
            isSignature = item.isSignature,
            isCosmetic = true
        )
        return penalty >= 0.7
    }
}
