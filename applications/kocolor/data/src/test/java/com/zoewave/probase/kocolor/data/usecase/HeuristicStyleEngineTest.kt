package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class HeuristicStyleEngineTest {

    private lateinit var engine: HeuristicStyleEngine

    @Before
    fun setUp() {
        val wardrobeRepo = mockk<WardrobeRepository>(relaxed = true)
        val cosmeticRepo = mockk<CosmeticInventoryRepository>(relaxed = true)
        val contextEngine = mockk<DeterministicContextEngine>(relaxed = true)
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
}
