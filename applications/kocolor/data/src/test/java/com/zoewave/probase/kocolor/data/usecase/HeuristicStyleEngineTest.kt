package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test

class HeuristicStyleEngineTest {

    private val wardrobeRepo = mockk<WardrobeRepository>(relaxed = true)
    private val cosmeticRepo = mockk<CosmeticInventoryRepository>(relaxed = true)
    private val contextEngine = mockk<DeterministicContextEngine>(relaxed = true)

    private lateinit var engine: HeuristicStyleEngine

    @Before
    fun setUp() {
        engine = HeuristicStyleEngine(wardrobeRepo, cosmeticRepo, contextEngine)
    }

    @Test
    fun smartPick_topDoesNotMatchStopSignTee() {
        val stopSignTee = ClothingItem(internalId = 1, name = "Stop Sign Red Tee", category = ClothingCategory.TOPS, colorHex = "#F00")
        val silkBlouse = ClothingItem(internalId = 2, name = "Silk Blouse", category = ClothingCategory.TOPS, colorHex = "#FFF")
        val items = listOf(stopSignTee, silkBlouse)

        val matchesForTop = engine.findMatches(items, "top", { it.name }, { it.notes })
        assertThat(matchesForTop).doesNotContain(stopSignTee)
        assertThat(matchesForTop).doesNotContain(silkBlouse)

        val matchesForSilk = engine.findMatches(items, "silk", { it.name }, { it.notes })
        assertThat(matchesForSilk).containsExactly(silkBlouse)
    }

    @Test
    fun generate_repeatedCalls_returnsIdenticalOutputAndRealHexesOnly() {
        val top = ClothingItem(internalId = 1, name = "Shirt", category = ClothingCategory.TOPS, colorHex = "#1E88E5")
        val bottom = ClothingItem(internalId = 2, name = "Pants", category = ClothingCategory.BOTTOMS, colorHex = "#D4AF37")
        val shoes = ClothingItem(internalId = 3, name = "Shoes", category = ClothingCategory.SHOES, colorHex = "#000000")
        val wardrobe = listOf(top, bottom, shoes)

        coEvery { wardrobeRepo.getAllClothing() } returns flowOf(wardrobe)
        coEvery { cosmeticRepo.getAllCosmetics() } returns flowOf(emptyList())

        val provs = wardrobe.map { CandidateProvenance(clothingItem = it, compositeScore = 0.8f) }
        coEvery { contextEngine.generateSelectionState(any(), any(), any()) } returns StyleSelectionState(fullRankedCandidatePool = provs)

        val context = StyleRequestContext(intent = "work")
        val blueprint1 = engine.generate(context)
        val blueprint2 = engine.generate(context)

        assertThat(blueprint1.selectedClothingIds).isEqualTo(blueprint2.selectedClothingIds)
        assertThat(blueprint1.recommendedPalette).isEqualTo(blueprint2.recommendedPalette)
        assertThat(blueprint1.recommendedPalette).containsExactly("#1E88E5", "#D4AF37", "#000000")
    }

    @Test
    fun generate_singleLockedDressAnchor_selectsDressAndShoesWithoutBottom() {
        val dress = ClothingItem(internalId = 10, name = "Dress", category = ClothingCategory.DRESSES, colorHex = "#FFF")
        val bottom = ClothingItem(internalId = 20, name = "Pants", category = ClothingCategory.BOTTOMS, colorHex = "#000")
        val shoes = ClothingItem(internalId = 30, name = "Heels", category = ClothingCategory.SHOES, colorHex = "#111")
        val wardrobe = listOf(dress, bottom, shoes)

        coEvery { wardrobeRepo.getAllClothing() } returns flowOf(wardrobe)
        coEvery { cosmeticRepo.getAllCosmetics() } returns flowOf(emptyList())

        val selectionState = StyleSelectionState(
            activeAnchors = listOf(dress),
            fullRankedCandidatePool = wardrobe.map { CandidateProvenance(clothingItem = it, compositeScore = 0.8f) }
        )
        coEvery { contextEngine.generateSelectionState(any(), any(), any()) } returns selectionState

        val context = StyleRequestContext(intent = "work", anchoredClothingIds = listOf("w_10"))
        val blueprint = engine.generate(context)

        assertThat(blueprint.selectedClothingIds).contains("w_10")
        assertThat(blueprint.selectedClothingIds).contains("w_30")
        assertThat(blueprint.selectedClothingIds).doesNotContain("w_20")
    }

    @Test
    fun generate_explicitDualLock_preservesBothDressAndBottom() {
        val dress = ClothingItem(internalId = 10, name = "Dress", category = ClothingCategory.DRESSES, colorHex = "#FFF")
        val bottom = ClothingItem(internalId = 20, name = "Pants", category = ClothingCategory.BOTTOMS, colorHex = "#000")
        val wardrobe = listOf(dress, bottom)

        coEvery { wardrobeRepo.getAllClothing() } returns flowOf(wardrobe)
        coEvery { cosmeticRepo.getAllCosmetics() } returns flowOf(emptyList())

        val selectionState = StyleSelectionState(
            activeAnchors = listOf(dress, bottom),
            fullRankedCandidatePool = wardrobe.map { CandidateProvenance(clothingItem = it, compositeScore = 0.8f) }
        )
        coEvery { contextEngine.generateSelectionState(any(), any(), any()) } returns selectionState

        val context = StyleRequestContext(intent = "work", anchoredClothingIds = listOf("w_10", "w_20"))
        val blueprint = engine.generate(context)

        assertThat(blueprint.selectedClothingIds).contains("w_10")
        assertThat(blueprint.selectedClothingIds).contains("w_20")
    }
}
