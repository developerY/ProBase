package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.util.KeywordMatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeuristicStyleEngine @Inject constructor(
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val contextEngine: DeterministicContextEngine
) : DeterministicStyleEngine {

    internal fun <T> findMatches(items: List<T>, intent: String, nameSelector: (T) -> String, notesSelector: (T) -> String?): List<T> {
        return items.filter { item ->
            KeywordMatcher.hasAnyMatchingToken("${nameSelector(item)} ${notesSelector(item).orEmpty()}", intent)
        }
    }

    override fun generate(context: StyleRequestContext): StyleBlueprint {
        val availableWardrobe = runBlocking { wardrobeRepository.getAllClothing().first() }
        val availableCosmetics = runBlocking { cosmeticRepository.getAllCosmetics().first() }

        val selectedItems = mutableListOf<ClothingItem>()
        val selectedCosmetics = mutableListOf<CosmeticItem>()
        
        // 1. Resolve Active Anchors via ContextEngine
        val selectionState = runBlocking { contextEngine.generateSelectionState(availableWardrobe, context.lockedConstraints, context) }
        val activeAnchors = selectionState.activeAnchors.ifEmpty {
            val anchoredIds = context.anchoredClothingIds.toSet()
            availableWardrobe.filter { "w_${it.internalId}" in anchoredIds || it.remoteId in anchoredIds }
        }
        selectedItems.addAll(activeAnchors)

        val scoreById: Map<Long, Float> = selectionState.fullRankedCandidatePool
            .mapNotNull { prov -> prov.clothingItem?.let { it.internalId to prov.compositeScore } }
            .toMap()

        val anchorCategories = activeAnchors.map { it.category }.toSet()
        val hasAnchoredDress = ClothingCategory.DRESSES in anchorCategories
        val hasAnchoredTop = ClothingCategory.TOPS in anchorCategories
        val hasAnchoredBottom = ClothingCategory.BOTTOMS in anchorCategories
        val hasAnchoredShoes = ClothingCategory.SHOES in anchorCategories

        fun <T> List<T>.deterministicPick(nameSelector: (T) -> String, notesSelector: (T) -> String?, idSelector: (T) -> Long): T? {
            if (this.isEmpty()) return null
            val matches = findMatches(this, context.intent, nameSelector, notesSelector)
            val pool = if (matches.isNotEmpty()) matches else this
            return pool.sortedWith(
                compareByDescending<T> { scoreById[idSelector(it)] ?: 0f }
                    .thenBy { idSelector(it) }
            ).firstOrNull()
        }

        // 2. Dress-aware slot resolution
        val isExplicitDualLock = hasAnchoredDress && (hasAnchoredTop || hasAnchoredBottom)

        if (!hasAnchoredDress || isExplicitDualLock) {
            val eligibleDresses = availableWardrobe.filter { it.category == ClothingCategory.DRESSES && it !in selectedItems }
            val eligibleTops = availableWardrobe.filter { (it.category == ClothingCategory.TOPS || it.category == ClothingCategory.ACTIVEWEAR) && it !in selectedItems }
            val eligibleBottoms = availableWardrobe.filter { it.category == ClothingCategory.BOTTOMS && it !in selectedItems }

            val bestDress = eligibleDresses.deterministicPick({ it.name }, { it.notes }, { it.internalId })
            val bestTop = eligibleTops.deterministicPick({ it.name }, { it.notes }, { it.internalId })
            val bestBottom = eligibleBottoms.deterministicPick({ it.name }, { it.notes }, { it.internalId })

            val dressScore = bestDress?.let { scoreById[it.internalId] ?: 0f } ?: -1f
            val topScore = bestTop?.let { scoreById[it.internalId] ?: 0f } ?: 0f
            val bottomScore = bestBottom?.let { scoreById[it.internalId] ?: 0f } ?: 0f
            val twoPieceAvgScore = if (bestTop != null && bestBottom != null) (topScore + bottomScore) / 2f else 0f

            val useDress = !hasAnchoredTop && !hasAnchoredBottom && bestDress != null && (
                bestTop == null || bestBottom == null || dressScore > twoPieceAvgScore
            )

            if (useDress && bestDress != null) {
                selectedItems.add(bestDress)
            } else {
                if (!hasAnchoredTop && bestTop != null) {
                    selectedItems.add(bestTop)
                }
                if (!hasAnchoredBottom && bestBottom != null) {
                    selectedItems.add(bestBottom)
                }
            }
        }

        if (!hasAnchoredShoes) {
            val shoes = availableWardrobe.filter { it.category == ClothingCategory.SHOES && it !in selectedItems }
            shoes.deterministicPick({ it.name }, { it.notes }, { it.internalId })?.let { selectedItems.add(it) }
        }

        // 3. Pick Cosmetics deterministically (Eyes, Cheeks, Lips, Nails)
        val eyes = availableCosmetics.filter { it.macroCategory == MacroCategory.EYES }
        val cheeks = availableCosmetics.filter { it.macroCategory == MacroCategory.DIMENSION }
        val lips = availableCosmetics.filter { it.macroCategory == MacroCategory.LIPS }
        val nails = availableCosmetics.filter { it.macroCategory == MacroCategory.NAILS }

        eyes.deterministicPick({ it.name }, { it.notes }, { it.internalId })?.let { selectedCosmetics.add(it) }
        cheeks.deterministicPick({ it.name }, { it.notes }, { it.internalId })?.let { selectedCosmetics.add(it) }
        lips.deterministicPick({ it.name }, { it.notes }, { it.internalId })?.let { selectedCosmetics.add(it) }
        nails.deterministicPick({ it.name }, { it.notes }, { it.internalId })?.let { selectedCosmetics.add(it) }

        // 4. Real Hex Palette Only (no random hex padding)
        val palette = (selectedItems.map { it.colorHex } + selectedCosmetics.map { it.colorHex })
            .filter { it.isNotBlank() }
            .distinct()
            .take(4)

        return StyleBlueprint(
            rationale = "Local Architect: Selected from your vault based on intent and availability.",
            selectedClothingIds = selectedItems.map { "w_${it.internalId}" },
            selectedCosmeticIds = selectedCosmetics.map { "c_${it.internalId}" },
            recommendedPalette = palette
        )
    }
}
