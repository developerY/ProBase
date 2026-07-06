package com.zoewave.probase.kocolor.features.analyzer.simulator.data

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import io.mockk.mockk
import org.junit.Test

class StyleSimulatorEngineTest {

    private val localAi = mockk<com.zoewave.probase.features.ai.local.data.LocalAiEngine>()
    private val engine = StyleSimulatorEngine(localAi)

    @Test
    fun `architectLocalBlueprint should select at least one item and generate palette`() {
        val wardrobe = listOf(
            ClothingItem(id = 1, name = "Silk Top", category = ClothingCategory.TOPS, dominantHex = "#FF0000"),
            ClothingItem(id = 2, name = "Jeans", category = ClothingCategory.BOTTOMS, dominantHex = "#0000FF"),
            ClothingItem(id = 3, name = "Sneakers", category = ClothingCategory.SHOES, dominantHex = "#000000")
        )

        val blueprint = engine.architectLocalBlueprint("fancy night", wardrobe)

        assertThat(blueprint.selectedItemIds).isNotEmpty()
        assertThat(blueprint.recommendedPalette).hasSize(3)
        assertThat(blueprint.rationale).contains("Local Architect")
    }

    @Test
    fun `architectLocalBlueprint should match keywords in user intent`() {
        val wardrobe = listOf(
            ClothingItem(id = 1, name = "Business Shirt", category = ClothingCategory.TOPS),
            ClothingItem(id = 2, name = "Casual Tee", category = ClothingCategory.TOPS),
            ClothingItem(id = 3, name = "Dress Pants", category = ClothingCategory.BOTTOMS)
        )

        val blueprint = engine.architectLocalBlueprint("business formal", wardrobe)

        assertThat(blueprint.selectedItemIds).contains(1L)
    }
}
