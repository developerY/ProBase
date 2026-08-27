package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import org.junit.Before
import org.junit.Test

class GreedyRehydratorTest {

    private lateinit var rehydrator: GreedyRehydrator

    @Before
    fun setup() {
        rehydrator = GreedyRehydrator()
    }

    @Test
    fun `hallucination test - forced clothing and cosmetic anchors omitted by AI are re-injected`() {
        val forcedJacket = ClothingItem(
            internalId = 101,
            remoteId = "w_101",
            name = "Forced Leather Jacket",
            category = ClothingCategory.OUTERWEAR,
            colorHex = "#000000"
        )
        val forcedLipstick = CosmeticItem(
            internalId = 201,
            remoteId = "c_201",
            name = "Signature Crimson Lip",
            brand = "KoColor",
            macroCategory = MacroCategory.LIPS,
            microCategory = MicroCategory.LIPSTICK,
            colorHex = "#FF0000"
        )

        val inventory = listOf(forcedJacket)
        val cosmetics = listOf(forcedLipstick)

        // Mock AI hallucination response that completely omits w_101 and c_201
        val aiClothingIds = listOf<String>()
        val aiCosmeticIds = listOf<String>()

        val result = rehydrator.mapToVisualBlueprintData(
            aiSelectedClothingIds = aiClothingIds,
            aiSelectedCosmeticIds = aiCosmeticIds,
            inventory = inventory,
            cosmetics = cosmetics,
            activeClothingAnchors = listOf(forcedJacket),
            activeCosmeticAnchors = listOf(forcedLipstick),
            isComplete = true
        )

        // Assert fail-safe forcibly re-injected both missing anchors into their respective slots
        assertThat(result.outerwearItem).isNotNull()
        assertThat(result.outerwearItem?.name).isEqualTo("Forced Leather Jacket")

        assertThat(result.lipsItem).isNotNull()
        assertThat(result.lipsItem?.name).isEqualTo("Signature Crimson Lip")
    }

    @Test
    fun `activewear fallback test - generic activewear item with Yoga Pants name is mapped to bottomItem`() {
        val activewearPants = ClothingItem(
            internalId = 50,
            remoteId = "w_50",
            name = "Flex Stretch Yoga Pants",
            category = ClothingCategory.ACTIVEWEAR,
            colorHex = "#333333"
        )

        val inventory = listOf(activewearPants)
        val aiClothingIds = listOf("w_50")

        val result = rehydrator.mapToVisualBlueprintData(
            aiSelectedClothingIds = aiClothingIds,
            aiSelectedCosmeticIds = emptyList(),
            inventory = inventory,
            cosmetics = emptyList(),
            isComplete = true
        )

        assertThat(result.bottomItem).isNotNull()
        assertThat(result.bottomItem?.name).isEqualTo("Flex Stretch Yoga Pants")
    }
}
