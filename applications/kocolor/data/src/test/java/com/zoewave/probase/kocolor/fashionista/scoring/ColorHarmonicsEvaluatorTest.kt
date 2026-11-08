package com.zoewave.probase.kocolor.fashionista.scoring

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ColorHarmonicsEvaluatorTest {

    private lateinit var evaluator: ColorHarmonicsEvaluator
    private val context = StyleRequestContext(
        intent = "work",
        appearanceProfile = com.zoewave.probase.kocolor.data.usecase.AppearanceProfile(undertone = com.zoewave.probase.core.model.ritual.Undertone.NEUTRAL.name, depth = "Medium", contrast = "Balanced")
    )

    @Before
    fun setUp() {
        evaluator = ColorHarmonicsEvaluator()
    }

    @Test
    fun emptyOutfit_returnsZero() {
        val blueprint = StyleBlueprint(recommendedPalette = emptyList())
        val score = evaluator.evaluate(blueprint, context, emptyList())
        assertThat(score).isEqualTo(0f)
    }

    @Test
    fun poorOrClashingInput_scoresBelow45() {
        // Red and Green Clash
        val blueprint = StyleBlueprint(recommendedPalette = listOf("#FF0000", "#00FF00"))
        val clothing = listOf(
            ClothingItem(internalId = 1, name = "Red", category = ClothingCategory.TOPS, colorHex = "#FF0000", colorTemperature = "Warm"),
            ClothingItem(internalId = 2, name = "Green", category = ClothingCategory.BOTTOMS, colorHex = "#00FF00", colorTemperature = "Cool")
        )
        val score = evaluator.evaluate(blueprint, context, clothing)
        assertThat(score).isLessThan(45f)
    }

    @Test
    fun neutralInput_scoresBetween50And68() {
        // Grays
        val blueprint = StyleBlueprint(recommendedPalette = emptyList())
        val clothing = listOf(
            ClothingItem(internalId = 1, name = "Gray", category = ClothingCategory.TOPS, colorHex = "#808080", colorTemperature = "Neutral"),
            ClothingItem(internalId = 2, name = "Light Gray", category = ClothingCategory.BOTTOMS, colorHex = "#A0A0A0", colorTemperature = "Neutral")
        )
        val score = evaluator.evaluate(blueprint, context, clothing)
        assertThat(score).isAtLeast(50f)
        assertThat(score).isAtMost(68f)
    }

    @Test
    fun exceptionalInput_scoresAtLeast95() {
        val warmContext = StyleRequestContext(
            intent = "work",
            appearanceProfile = com.zoewave.probase.kocolor.data.usecase.AppearanceProfile(undertone = com.zoewave.probase.core.model.ritual.Undertone.WARM.name, depth = "Medium", contrast = "Balanced")
        )
        // High contrast tonal warm
        val blueprint = StyleBlueprint(recommendedPalette = listOf("#FF0000", "#FFC0CB"))
        val clothing = listOf(
            ClothingItem(internalId = 1, name = "Red", category = ClothingCategory.TOPS, colorHex = "#FF0000", colorTemperature = "Warm"),
            ClothingItem(internalId = 2, name = "Pink", category = ClothingCategory.BOTTOMS, colorHex = "#FFC0CB", colorTemperature = "Warm")
        )
        val score = evaluator.evaluate(blueprint, warmContext, clothing)
        assertThat(score).isAtLeast(95f)
    }

    @Test
    fun singleChromaLightnessCurve_matchesAllSevenControlPoints() {
        assertThat(evaluator.evaluateNeutralLightnessModifier(0f)).isEqualTo(-15f)
        assertThat(evaluator.evaluateNeutralLightnessModifier(12f)).isEqualTo(5f)
        assertThat(evaluator.evaluateNeutralLightnessModifier(30f)).isEqualTo(25f)
        assertThat(evaluator.evaluateNeutralLightnessModifier(50f)).isEqualTo(38f)
        assertThat(evaluator.evaluateNeutralLightnessModifier(65f)).isEqualTo(30f)
        assertThat(evaluator.evaluateNeutralLightnessModifier(75f)).isEqualTo(20f)
        assertThat(evaluator.evaluateNeutralLightnessModifier(90f)).isEqualTo(15f)
    }
}
