package com.zoewave.probase.kocolor.fashionista.scoring

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.usecase.AppearanceProfile
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FashionistaEngineTest {

    private lateinit var engine: FashionistaEngine
    private val context = StyleRequestContext(
        intent = "work",
        appearanceProfile = AppearanceProfile(undertone = "Warm", depth = "Deep", contrast = "High")
    )

    @Before
    fun setUp() {
        val colorHarmonics = ColorHarmonicsEvaluator()
        val silhouette = SilhouetteGrader()
        val contrastAnchor = ContrastAnchorEvaluator()
        engine = FashionistaEngine(colorHarmonics, silhouette, contrastAnchor)
    }

    @Test
    fun emptyOutfit_returnsZeroAndUnapproved() {
        val blueprint = StyleBlueprint(selectedClothingIds = emptyList())
        val score = engine.evaluate(blueprint, context, emptyList())
        assertThat(score.totalScore).isEqualTo(0f)
        assertThat(score.isApproved).isFalse()
    }

    @Test
    fun clashingUnbalancedOutfit_scoresBelow60AndFailsGate() {
        val clothing = listOf(
            ClothingItem(internalId = 1, name = "Oversized Sweater", category = ClothingCategory.TOPS, colorHex = "#FF0000", colorTemperature = "Warm"),
            ClothingItem(internalId = 2, name = "Wide Pants", category = ClothingCategory.BOTTOMS, colorHex = "#00FF00", colorTemperature = "Cool") // Clash + Boxy
        )
        val blueprint = StyleBlueprint(
            selectedClothingIds = listOf("w_1", "w_2"),
            recommendedPalette = listOf("#FF0000", "#00FF00")
        )

        val lightContext = StyleRequestContext(
            intent = "work",
            appearanceProfile = AppearanceProfile(undertone = "Cool", depth = "Light", contrast = "Low")
        )

        val score = engine.evaluate(blueprint, lightContext, clothing)
        assertThat(score.totalScore).isLessThan(60f)
        assertThat(score.isApproved).isFalse()
    }

    @Test
    fun goodEverydayOutfit_scoresBetween80And89AndPassesGate() {
        val clothing = listOf(
            ClothingItem(internalId = 1, name = "Cropped Slim Top", category = ClothingCategory.TOPS, colorHex = "#808080", colorTemperature = "Neutral"),
            ClothingItem(internalId = 2, name = "High Waisted Jeans", category = ClothingCategory.BOTTOMS, colorHex = "#A0A0A0", colorTemperature = "Neutral"),
            ClothingItem(internalId = 3, name = "Jacket", category = ClothingCategory.OUTERWEAR, colorHex = "#808080", colorTemperature = "Neutral")
        )
        val blueprint = StyleBlueprint(
            selectedClothingIds = listOf("w_1", "w_2", "w_3"),
            recommendedPalette = listOf("#808080", "#A0A0A0")
        )

        val neutralContext = StyleRequestContext(
            intent = "work",
            appearanceProfile = AppearanceProfile(undertone = "Neutral", depth = "Medium", contrast = "Balanced")
        )

        val score = engine.evaluate(blueprint, neutralContext, clothing)
        assertThat(score.totalScore).isAtLeast(80f)
        assertThat(score.totalScore).isAtMost(89f)
        assertThat(score.isApproved).isTrue()
    }

    @Test
    fun exceptionalEditorialOutfit_scoresAtLeast95() {
        val clothing = listOf(
            ClothingItem(internalId = 1, name = "Cropped Slim Top", category = ClothingCategory.TOPS, colorHex = "#000000", colorTemperature = "Neutral"),
            ClothingItem(internalId = 2, name = "High Waisted Wide Leg Trousers", category = ClothingCategory.BOTTOMS, colorHex = "#FFFFFF", colorTemperature = "Neutral"),
            ClothingItem(internalId = 3, name = "Tailored Overcoat", category = ClothingCategory.OUTERWEAR, colorHex = "#000000", colorTemperature = "Neutral")
        )
        val blueprint = StyleBlueprint(
            selectedClothingIds = listOf("w_1", "w_2", "w_3"),
            recommendedPalette = listOf("#000000", "#FFFFFF")
        )

        val deepHighContrastContext = StyleRequestContext(
            intent = "work",
            appearanceProfile = AppearanceProfile(undertone = "Neutral", depth = "Deep", contrast = "High")
        )

        val score = engine.evaluate(blueprint, deepHighContrastContext, clothing)
        assertThat(score.totalScore).isAtLeast(95f)
        assertThat(score.isApproved).isTrue()
    }
}
