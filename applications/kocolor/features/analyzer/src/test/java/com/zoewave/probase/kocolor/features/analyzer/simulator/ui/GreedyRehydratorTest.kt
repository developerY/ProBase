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
    fun mapToVisualBlueprintData_pureUiMapping_preservesUpstreamSelections() {
        val jacket = ClothingItem(
            internalId = 101,
            remoteId = "w_101",
            name = "Leather Jacket",
            category = ClothingCategory.OUTERWEAR,
            colorHex = "#000000"
        )
        val lipstick = CosmeticItem(
            internalId = 201,
            remoteId = "c_201",
            name = "Crimson Lip",
            brand = "KoColor",
            macroCategory = MacroCategory.LIPS,
            microCategory = MicroCategory.LIPSTICK,
            colorHex = "#FF0000"
        )

        val inventory = listOf(jacket)
        val cosmetics = listOf(lipstick)

        val aiClothingIds = listOf("w_101")
        val aiCosmeticIds = listOf("c_201")

        val result = rehydrator.mapToVisualBlueprintData(
            aiSelectedClothingIds = aiClothingIds,
            aiSelectedCosmeticIds = aiCosmeticIds,
            inventory = inventory,
            cosmetics = cosmetics,
            palette = emptyList(),
            fashionistaScore = 95,
            isComplete = true
        )

        assertThat(result.outerwearItem).isNotNull()
        assertThat(result.outerwearItem?.name).isEqualTo("Leather Jacket")
        assertThat(result.lipsItem).isNotNull()
        assertThat(result.lipsItem?.name).isEqualTo("Crimson Lip")
        assertThat(result.koColorScore).isEqualTo(95)
    }
}
