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
class ContrastAnchorEvaluatorTest {

    private lateinit var evaluator: ContrastAnchorEvaluator

    @Before
    fun setUp() {
        evaluator = ContrastAnchorEvaluator()
    }

    @Test
    fun emptyOutfit_returnsZero() {
        val context = StyleRequestContext(intent = "work")
        val score = evaluator.evaluate(StyleBlueprint(recommendedPalette = emptyList()), context, emptyList())
        assertThat(score).isEqualTo(0f)
    }

    @Test
    fun poorOrClashingInput_scoresBelow45() {
        val context = StyleRequestContext(
            intent = "work",
            appearanceProfile = com.zoewave.probase.kocolor.data.usecase.AppearanceProfile(
                undertone = com.zoewave.probase.core.model.ritual.Undertone.NEUTRAL.name,
                depth = "Light",
                contrast = "Low"
            )
        )
        // High contrast dark outfit + Light depth user -> Overpowering + Contrast clash
        val blueprint = StyleBlueprint(recommendedPalette = listOf("#000000", "#FFFFFF"), selectedClothingIds = listOf("w_1"))
        val score = evaluator.evaluate(blueprint, context, listOf(ClothingItem(internalId = 1, name = "C", category = ClothingCategory.TOPS, colorHex = "#000000")))
        assertThat(score).isLessThan(45f)
    }

    @Test
    fun neutralInput_scoresBetween50And68() {
        val context = StyleRequestContext(
            intent = "work",
            appearanceProfile = com.zoewave.probase.kocolor.data.usecase.AppearanceProfile(
                undertone = com.zoewave.probase.core.model.ritual.Undertone.NEUTRAL.name,
                depth = "Medium",
                contrast = "Balanced"
            )
        )
        // Balanced medium contrast
        val blueprint = StyleBlueprint(recommendedPalette = listOf("#808080", "#C0C0C0"), selectedClothingIds = listOf("w_1"))
        val score = evaluator.evaluate(blueprint, context, listOf(ClothingItem(internalId = 1, name = "C", category = ClothingCategory.TOPS, colorHex = "#808080")))
        assertThat(score).isAtLeast(50f)
        assertThat(score).isAtMost(68f)
    }

    @Test
    fun exceptionalInput_scoresAtLeast95() {
        val context = StyleRequestContext(
            intent = "work",
            appearanceProfile = com.zoewave.probase.kocolor.data.usecase.AppearanceProfile(
                undertone = com.zoewave.probase.core.model.ritual.Undertone.NEUTRAL.name,
                depth = "Deep",
                contrast = "High"
            )
        )
        // High contrast deep outfit + High contrast deep user
        val blueprint = StyleBlueprint(recommendedPalette = listOf("#000000", "#FFFFFF"), selectedClothingIds = listOf("w_1"))
        val score = evaluator.evaluate(blueprint, context, listOf(ClothingItem(internalId = 1, name = "C", category = ClothingCategory.TOPS, colorHex = "#000000")))
        assertThat(score).isAtLeast(95f)
    }
}
