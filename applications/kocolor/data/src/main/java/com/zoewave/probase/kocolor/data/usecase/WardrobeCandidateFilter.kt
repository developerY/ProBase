package com.zoewave.probase.kocolor.data.usecase

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.Temperature
import com.zoewave.probase.core.util.color.ColorQuantizer
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.color.RecommendationWeights
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

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
        var score = 1.0
        val tempC = context.weatherTempC ?: 22f

        // 1. Appearance Temperature Harmony Bonus
        val appearanceString = "${context.appearanceProfile.undertone} • ${context.appearanceProfile.depth} • ${context.appearanceProfile.contrast}"
        val itemTemp = item.colorTemperature ?: "Neutral"
        if (appearanceString.contains(itemTemp, ignoreCase = true)) {
            score += RecommendationWeights.APPEARANCE_TEMPERATURE_HARMONY_BONUS
        } else if (itemTemp.equals("Neutral", ignoreCase = true)) {
            score += RecommendationWeights.NEUTRAL_ANCHOR_BONUS
        }

        // 2. Weather & Thermal Mismatch Penalty (> 20.0°C vs Heavy/Insulating Constructions)
        val nameLower = item.name.lowercase()
        val materialLower = (item.material ?: "").lowercase()

        // Cold-weather & insulating constructions (fleece, shearling, down, velvet, heavy wool)
        val isInsulatingConstruction = nameLower.contains("velvet") || nameLower.contains("shearling") ||
                nameLower.contains("wool") || nameLower.contains("fleece") || nameLower.contains("down") ||
                materialLower.contains("velvet") || materialLower.contains("shearling") || materialLower.contains("wool") || materialLower.contains("fleece")

        // Warm-weather-compatible materials (linen, lightweight cotton, silk, seersucker)
        val isWarmWeatherMaterial = nameLower.contains("linen") || nameLower.contains("cotton") ||
                nameLower.contains("silk") || nameLower.contains("seersucker") ||
                materialLower.contains("linen") || materialLower.contains("silk") || materialLower.contains("seersucker")

        if (tempC > 20.0f && isInsulatingConstruction) {
            score += RecommendationWeights.THERMAL_MISMATCH_PENALTY // -3.0
        } else if (tempC > 20.0f && isWarmWeatherMaterial) {
            score += RecommendationWeights.WEATHER_ALIGNMENT_BONUS // +1.5
        }

        // 3. Pre-Gemini High-Chroma Intent Boost & Neutral Demotion
        val chroma = calculateChroma(item.colorHex)
        if (context.intentProfile.colorfulness > 0.7f) {
            if (chroma > 30.0f) {
                score += RecommendationWeights.HIGH_CHROMA_INTENT_BONUS // +2.5f
            } else if (chroma < 15.0f) {
                score += RecommendationWeights.MONOCHROME_NEUTRAL_PENALTY // -1.5f
            }
        }

        // 4. Score contextual relevance against user intent keywords and occasion
        val keywords = context.intent.lowercase().split(" ", ",", ".")
        if (keywords.any { item.name.contains(it, ignoreCase = true) || (item.notes?.contains(it, ignoreCase = true) ?: false) }) {
            score += 2.0
        }

        return score
    }

    private fun calculateChroma(hex: String?): Float {
        if (hex.isNullOrBlank()) return 0f
        return try {
            val colorInt = Color.parseColor(hex)
            val lab = DoubleArray(3)
            ColorUtils.colorToLAB(colorInt, lab)
            val a = lab[1]
            val b = lab[2]
            sqrt(a * a + b * b).toFloat()
        } catch (e: Exception) {
            0f
        }
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
        val rankedRemainingProv = remainingItems.map { rawItem ->
            val effectiveTemp = if (rawItem.temperature != Temperature.UNKNOWN) {
                rawItem.temperature
            } else {
                ColorQuantizer.determineTemperature(rawItem.colorHex)
            }
            val item = if (rawItem.temperature != Temperature.UNKNOWN) rawItem else rawItem.copy(temperature = effectiveTemp)
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

        val anchoredProv = anchoredItems.map { rawItem ->
            val effectiveTemp = if (rawItem.temperature != Temperature.UNKNOWN) {
                rawItem.temperature
            } else {
                ColorQuantizer.determineTemperature(rawItem.colorHex)
            }
            val item = if (rawItem.temperature != Temperature.UNKNOWN) rawItem else rawItem.copy(temperature = effectiveTemp)
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

        val effectiveTemp = if (item.temperature != Temperature.UNKNOWN) {
            item.temperature
        } else {
            ColorQuantizer.determineTemperature(item.colorHex)
        }

        val cosmeticTemp = effectiveTemp.name.uppercase()
        when {
            // Appearance Temperature Harmony: Warm cosmetic in Warm context OR Cool in Cool context
            (isWarmContext && cosmeticTemp.contains("WARM")) ||
            (isCoolContext && cosmeticTemp.contains("COOL")) -> {
                score += RecommendationWeights.APPEARANCE_TEMPERATURE_HARMONY_BONUS // +2.0
            }
            // Appearance Temperature Clash Penalty: Cool cosmetic in Warm context OR Warm in Cool context
            (isWarmContext && cosmeticTemp.contains("COOL")) ||
            (isCoolContext && cosmeticTemp.contains("WARM")) -> {
                score += RecommendationWeights.APPEARANCE_TEMPERATURE_CLASH_PENALTY // -2.5
            }
            // Neutral Anchor Bonus
            cosmeticTemp.contains("NEUTRAL") -> {
                score += RecommendationWeights.NEUTRAL_ANCHOR_BONUS // +1.25
            }
            else -> score += 0.50
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
