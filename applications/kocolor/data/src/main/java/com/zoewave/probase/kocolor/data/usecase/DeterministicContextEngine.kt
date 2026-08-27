package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.color.ColorHarmonyEngine
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.telemetry.AnchorSource
import com.zoewave.probase.kocolor.data.telemetry.PruningRecord
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeterministicContextEngine @Inject constructor(
    private val colorEngine: ColorHarmonyEngine,
    private val rotationScoringUseCase: RotationScoringUseCase,
    private val repository: WardrobeRepository,
    private val auditLogger: StyleAuditLogger
) {

    /**
     * Builds the "Reasoning Set" for the AI by performing deterministic retrieval and color scoring.
     */
    suspend fun buildReasoningSet(
        inventory: List<ClothingItem>,
        context: StyleRequestContext,
        limit: Int
    ): List<CandidateProvenance> {
        val initialCount = inventory.size

        // Phase 1: Anchor Selection
        val anchor = selectAnchor(inventory, context) ?: return emptyList()

        // Phase 2: Hard Constraints & Filtering
        val afterWeather = inventory.filter { item ->
            // Simulating steps for pruning log
            val isHot = context.weather.contains("Temp: 2", ignoreCase = true) || context.weather.contains("Temp: 3", ignoreCase = true)
            !(isHot && item.category == ClothingCategory.OUTERWEAR)
        }.size

        val eligibleItems = inventory.filter { item ->
            item.id != anchor.id && 
            isContextuallyViable(item, context)
        }
        
        auditLogger.logDeterministicPruning(
            context.requestId,
            PruningRecord(
                initialCount = initialCount,
                afterWeather = afterWeather,
                afterRotation = eligibleItems.size,
                finalEligible = eligibleItems.size
            )
        )

        // Phase 3: Continuous Scoring
        val anchorHsl = colorEngine.hexToHsl(anchor.colorHex)
        val scoredCandidates = eligibleItems.map { item ->
            val candidateHsl = colorEngine.hexToHsl(item.colorHex)
            
            val colorScore = colorEngine.calculateCompatibility(anchorHsl, candidateHsl, context.appearanceTelemetry)
            val contextScore = calculateContextScore(item, context)
            val freshnessScore = calculateFreshnessScore(item, context)
            
            val baseReason = "Mathematically harmonic with ${anchor.name}"
            val finalReason = if (item.isSignature) "[Signature Item] Rotation bypassed. $baseReason" else baseReason

            CandidateProvenance(
                clothingItem = item,
                contextScore = contextScore,
                colorScore = colorScore,
                appearanceScore = 0.8f, // Matching appearance profile logic
                freshnessScore = freshnessScore,
                retrievalReason = finalReason
            )
        }

        // Phase 4: Adaptive Role-Aware Diversity
        val diverseCandidates = enforceRoleDiversity(anchor, scoredCandidates, context)

        // Phase 5: Truncate & Prepend Anchor
        val anchorProvenance = CandidateProvenance(clothingItem = anchor, contextScore = 1f, colorScore = 1f, appearanceScore = 1f, freshnessScore = 1f, retrievalReason = "Primary Anchor")
        return (listOf(anchorProvenance) + diverseCandidates).take(limit)
    }

    private fun selectAnchor(inventory: List<ClothingItem>, context: StyleRequestContext): ClothingItem? {
        // 1. User-Locked Item
        context.userLockedAnchorId?.let { id ->
            inventory.find { "w_${it.internalId}" == id }?.let { 
                auditLogger.logAnchorResolution(context.requestId, it, AnchorSource.USER_LOCKED, "Explicitly forced by user")
                return it 
            }
        }
        
        // 2. User-Selected Item
        context.userSelectedAnchorId?.let { id ->
            inventory.find { "w_${it.internalId}" == id }?.let { 
                auditLogger.logAnchorResolution(context.requestId, it, AnchorSource.USER_SELECTED, "Manually selected in UI")
                return it 
            }
        }
        
        // 3. Automatic Anchor (pass hard constraints)
        val anchor = inventory.filter { isContextuallyViable(it, context) }
            .sortedByDescending { calculateContextScore(it, context) + calculateFreshnessScore(it, context) }
            .firstOrNull()
            
        anchor?.let {
            auditLogger.logAnchorResolution(context.requestId, it, AnchorSource.AUTOMATIC_CONTEXT, "Highest context + freshness score")
        }
        
        return anchor
    }

    private fun isContextuallyViable(item: ClothingItem, context: StyleRequestContext): Boolean {
        // Weather Gating, Laundry/Hidden, Rotation Lockout
        if (item.isHidden) return false
        
        // Bypass rotation penalty for signature items
        if (!item.isSignature) {
            val penalty = context.rotationScores[item.remoteId] ?: 0.0
            if (penalty >= 0.7) return false
        }
        
        // Basic weather check
        val isHot = context.weather.contains("Temp: 2", ignoreCase = true) || context.weather.contains("Temp: 3", ignoreCase = true)
        if (isHot && item.category == ClothingCategory.OUTERWEAR) return false
        
        return true
    }

    private fun calculateContextScore(item: ClothingItem, context: StyleRequestContext): Float {
        var score = 0.5f
        if (context.intent.contains(item.category.name, ignoreCase = true)) score += 0.3f
        return score.coerceIn(0f, 1f)
    }

    private fun calculateFreshnessScore(item: ClothingItem, context: StyleRequestContext): Float {
        val lastWorn = item.lastUsedTimestamp ?: 0L
        val daysSince = (System.currentTimeMillis() - lastWorn) / (24 * 60 * 60 * 1000L)
        return if (daysSince > 7) 1.0f else (daysSince / 7.0f).toFloat()
    }

    private fun enforceRoleDiversity(
        anchor: ClothingItem,
        candidates: List<CandidateProvenance>,
        context: StyleRequestContext
    ): List<CandidateProvenance> {
        val sorted = candidates.sortedByDescending { it.totalScore }
        val result = mutableListOf<CandidateProvenance>()
        
        val rolesNeeded = mutableSetOf(ClothingCategory.TOPS, ClothingCategory.BOTTOMS, ClothingCategory.SHOES)
        rolesNeeded.remove(anchor.category)
        
        // Greedy diversity selection
        for (prov in sorted) {
            val item = prov.clothingItem ?: continue
            if (item.category in rolesNeeded) {
                result.add(prov)
                // We keep needing more of a role until we have enough, say 3 per primary category
                if (result.count { it.clothingItem?.category == item.category } >= 3) {
                    rolesNeeded.remove(item.category)
                }
            } else if (result.size < 12) {
                result.add(prov)
            }
        }
        
        return result.sortedByDescending { it.totalScore }
    }
}

private val ClothingItem.id: String get() = "w_$internalId"
