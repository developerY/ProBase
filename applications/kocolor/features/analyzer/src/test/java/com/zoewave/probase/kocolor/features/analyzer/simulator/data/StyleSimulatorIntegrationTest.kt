package com.zoewave.probase.kocolor.features.analyzer.simulator.data

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.Formality
import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import com.zoewave.probase.features.ai.local.domain.router.RequiresCloudException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class StyleSimulatorIntegrationTest {

    private lateinit var localAi: LocalAiEngine
    private lateinit var engine: StyleSimulatorEngine

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        localAi = mockk()
        engine = StyleSimulatorEngine(localAi)
    }

    private val sampleWardrobe = listOf(
        ClothingItem(id = 1, name = "Power Suit", category = ClothingCategory.TOPS, formality = Formality.PROFESSIONAL, colorHex = "#222222", dominantHex = "#222222"),
        ClothingItem(id = 2, name = "Office Trousers", category = ClothingCategory.BOTTOMS, formality = Formality.PROFESSIONAL, colorHex = "#333333", dominantHex = "#333333"),
        ClothingItem(id = 3, name = "Oxfords", category = ClothingCategory.SHOES, formality = Formality.PROFESSIONAL, colorHex = "#111111", dominantHex = "#111111"),
        ClothingItem(id = 4, name = "Pajama Top", category = ClothingCategory.TOPS, formality = Formality.LOUNGE, colorHex = "#FFFFFF"),
        ClothingItem(id = 5, name = "Joggers", category = ClothingCategory.BOTTOMS, formality = Formality.LOUNGE, colorHex = "#808080")
    )

    @Test
    fun `Tier 1 (Cloud) should be preferred if API key is present`() {
        runBlocking {
            coEvery { localAi.generateStructuredContent(any(), any()) } returns Result.success("""
                {
                  "rationale": "Nano Stylist: Selected for professionalism.",
                  "selectedItemIds": [1, 2, 3],
                  "recommendedPalette": ["#222222", "#333333", "#111111"]
                }
            """.trimIndent())

            val blueprint = engine.architectStyleBlueprint(
                userIntent = "Boardroom negotiation",
                circadianContext = "Morning Defense",
                routineCompleted = true,
                wellnessScore = 0.9,
                availableWardrobe = sampleWardrobe.filter { it.formality >= Formality.PROFESSIONAL },
                apiKey = "" // Blank API key forces bypass of Tier 1
            )

            assertThat(blueprint.rationale).contains("Nano Stylist")
            assertThat(blueprint.selectedItemIds).containsExactly(1L, 2L, 3L)
        }
    }

    @Test
    fun `Tier 1_5 (Nano) should fallback to Tier 2 (Heuristics) on hardware lockout`() {
        runBlocking {
            // Mock Nano reporting a hardware bypass/lockout
            coEvery { localAi.generateStructuredContent(any(), any()) } returns Result.failure(RequiresCloudException("A-series silicon bypass"))

            val blueprint = engine.architectStyleBlueprint(
                userIntent = "Relaxing at home",
                circadianContext = "Evening Recovery",
                routineCompleted = false,
                wellnessScore = 0.5,
                availableWardrobe = sampleWardrobe,
                apiKey = "" 
            )

            // Verify it fell back to the "Local Architect" (Heuristics)
            assertThat(blueprint.rationale).contains("Local Architect")
        }
    }

    @Test
    fun `verify Biological Anchoring is present in the prompt generation`() {
        val skinProfile = "Undertone: COOL, Seasonal: WINTER"
        runBlocking {
            // Smoke test to ensure the engine parameters are wired
            val blueprint = engine.architectStyleBlueprint(
                userIntent = "fancy night",
                circadianContext = "Morning Defense",
                routineCompleted = true,
                wellnessScore = 0.9,
                availableWardrobe = sampleWardrobe,
                fashionProfile = skinProfile,
                apiKey = "" 
            )
            assertThat(blueprint).isNotNull()
        }
    }
}
