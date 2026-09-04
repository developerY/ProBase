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
    private val roleGapAnalyzer: RoleGapAnalyzer,
    private val rotationScoringUseCase: RotationScoringUseCase,
    private val repository: WardrobeRepository,
    private val auditLogger: StyleAuditLogger
) {

    /**
     * Generates a full [StyleSelectionState] with locked anchors, missing roles, composite profile,
     * and the fully ranked candidate pool.
     */
    suspend fun generateSelectionState(
        inventory: List<ClothingItem>,
        lockedConstraints: List<UserConstraint>,
        context: StyleRequestContext
    ): StyleSelectionState {
        val initialCount = inventory.size

        // 1. Resolve Anchors
        val lockedAnchors = resolveAnchors(inventory, lockedConstraints, context)
        
        // 2. Calculate Composite Profile from Anchors
        val compositeProfile = colorEngine.calculateCompositeProfile(lockedAnchors)

        // 3. Identify Missing Role Requirements
        val missingRoles = roleGapAnalyzer.determineRoleRequirements(
            anchors = lockedAnchors,
            occasion = context.occasion,
            weatherTempC = context.weatherTempC ?: 22f // Fallback for legacy role logic
        )

        // 4. Hard Constraints (Eliminate impossible items)
        val lockedIds = lockedAnchors.map { it.id }.toSet()
        val eligibleItems = inventory.filter { item ->
            item.id !in lockedIds && isContextuallyViable(item, context)
        }

        auditLogger.logDeterministicPruning(
            context.requestId,
            PruningRecord(
                initialCount = initialCount,
                afterWeather = eligibleItems.size,
                afterRotation = eligibleItems.size,
                finalEligible = eligibleItems.size
            )
        )

        // 5. Soft Scoring & Ranking
        val rankedPool = eligibleItems.map { item ->
            val lch = colorEngine.hexToLCh(item.colorHex)
            val colorScore = colorEngine.scoreCandidate(lch, compositeProfile, context.appearanceTelemetry)
            val contextScore = calculateContextScore(item, context)
            val freshnessScore = calculateFreshnessScore(item, context)

            val baseReason = if (lockedAnchors.isNotEmpty()) {
                "Harmonic with locked ${lockedAnchors.first().name}"
            } else {
                "Contextually compatible"
            }
            val finalReason = if (item.isSignature) "[Signature Item] Rotation bypassed. $baseReason" else baseReason

            CandidateProvenance(
                clothingItem = item,
                contextScore = contextScore,
                colorScore = colorScore,
                appearanceScore = 0.8f,
                freshnessScore = freshnessScore,
                compositeScore = (colorScore * 0.4f + contextScore * 0.3f + freshnessScore * 0.3f),
                retrievalReason = finalReason
            )
        }.sortedByDescending { it.compositeScore }

        return StyleSelectionState(
            activeAnchors = lockedAnchors,
            missingRoleRequirements = missingRoles,
            compositeProfile = compositeProfile,
            fullRankedCandidatePool = rankedPool
        )
    }

    private fun resolveAnchors(
        inventory: List<ClothingItem>,
        constraints: List<UserConstraint>,
        context: StyleRequestContext
    ): List<ClothingItem> {
        val anchors = mutableListOf<ClothingItem>()
        val lockedIds = constraints.map { it.itemId }.toSet()

        // 1. FORCED or LOCKED constraints from user
        constraints.forEach { constraint ->
            inventory.find { "w_${it.internalId}" == constraint.itemId || it.remoteId == constraint.itemId }?.let { item ->
                anchors.add(item)
                val tierName = constraint.tier.name
                auditLogger.logAnchorResolution(
                    context.requestId,
                    item,
                    if (constraint.tier == SelectionTier.FORCED) AnchorSource.USER_LOCKED else AnchorSource.USER_SELECTED,
                    "Constraint [$tierName] applied"
                )
            }
        }

        // 2. Free styling automatic anchor if no locked items exist
        if (anchors.isEmpty()) {
            selectAnchor(inventory, context)?.let { autoAnchor ->
                anchors.add(autoAnchor)
            }
        }

        return anchors.distinctBy { it.internalId }
    }

    private fun selectAnchor(inventory: List<ClothingItem>, context: StyleRequestContext): ClothingItem? {
        // 1. User-Locked or FORCED Item
        context.lockedConstraints.find { it.tier == SelectionTier.LOCKED || it.tier == SelectionTier.FORCED }?.let { constraint ->
            inventory.find { "w_${it.internalId}" == constraint.itemId || it.remoteId == constraint.itemId }?.let {
                auditLogger.logAnchorResolution(context.requestId, it, AnchorSource.USER_LOCKED, "Explicitly forced by user")
                return it
            }
        }
        
        // 2. User-Selected Item
        context.lockedConstraints.find { it.tier == SelectionTier.SELECTED }?.let { constraint ->
            inventory.find { "w_${it.internalId}" == constraint.itemId || it.remoteId == constraint.itemId }?.let {
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
