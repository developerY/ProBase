package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import io.mockk.mockk
import org.junit.Test

class StyleSimulatorEngineTest {

    private val localAi = mockk<LocalAiEngine>()
    private val engine = StyleSimulatorEngine(localAi)

    @Test
    fun `architectLocalBlueprint should select at least one item and generate palette`() {
        val wardrobe = listOf(
            ClothingItem(internalId = 1, remoteId = "1", name = "Silk Top", category = ClothingCategory.TOPS, colorHex = "#FF0000", dominantHex = "#FF0000"),
            ClothingItem(internalId = 2, remoteId = "2", name = "Jeans", category = ClothingCategory.BOTTOMS, colorHex = "#0000FF", dominantHex = "#0000FF"),
            ClothingItem(internalId = 3, remoteId = "3", name = "Sneakers", category = ClothingCategory.SHOES, colorHex = "#000000", dominantHex = "#000000")
        )

        val blueprint = engine.architectLocalBlueprint("fancy night", wardrobe, emptyList())

        assertThat(blueprint.selectedClothingIds).isNotEmpty()
        assertThat(blueprint.recommendedPalette).hasSize(4) // Engine pads to 4
        assertThat(blueprint.rationale).contains("Local Architect")
    }

    @Test
    fun `architectLocalBlueprint should match keywords in user intent`() {
        val wardrobe = listOf(
            ClothingItem(internalId = 1, remoteId = "1", name = "Business Shirt", category = ClothingCategory.TOPS, colorHex = "#FFFFFF"),
            ClothingItem(internalId = 2, remoteId = "2", name = "Casual Tee", category = ClothingCategory.TOPS, colorHex = "#FFFFFF"),
            ClothingItem(internalId = 3, remoteId = "3", name = "Dress Pants", category = ClothingCategory.BOTTOMS, colorHex = "#000000")
        )

        val blueprint = engine.architectLocalBlueprint("business formal", wardrobe, emptyList())

        assertThat(blueprint.selectedClothingIds).contains("w_1")
    }
}
