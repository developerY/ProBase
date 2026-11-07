package com.zoewave.probase.kocolor.fashionista.scoring

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import org.junit.Before
import org.junit.Test

class SilhouetteGraderTest {

    private lateinit var grader: SilhouetteGrader

    @Before
    fun setUp() {
        grader = SilhouetteGrader()
    }

    @Test
    fun emptyOutfit_returnsZero() {
        val blueprint = StyleBlueprint(selectedClothingIds = emptyList())
        val score = grader.grade(blueprint, emptyList())
        assertThat(score).isEqualTo(0f)
    }

    @Test
    fun poorOrClashingInput_scoresBelow45() {
        // Box penalty: Oversized top + oversized bottom
        val clothing = listOf(
            ClothingItem(name = "Oversized Sweater", category = ClothingCategory.TOPS, colorHex = "#000000"),
            ClothingItem(name = "Wide Leg Pants", category = ClothingCategory.BOTTOMS, colorHex = "#000000")
        )
        val score = grader.grade(StyleBlueprint(selectedClothingIds = listOf("w_1", "w_2")), clothing)
        assertThat(score).isLessThan(45f)
    }

    @Test
    fun neutralInput_scoresBetween50And68() {
        // Basic shirt + basic pants (no specific cut tags)
        val clothing = listOf(
            ClothingItem(name = "T-Shirt", category = ClothingCategory.TOPS, colorHex = "#000000"),
            ClothingItem(name = "Jeans", category = ClothingCategory.BOTTOMS, colorHex = "#000000")
        )
        val score = grader.grade(StyleBlueprint(selectedClothingIds = listOf("w_1", "w_2")), clothing)
        assertThat(score).isAtLeast(50f)
        assertThat(score).isAtMost(68f)
    }

    @Test
    fun exceptionalInput_scoresAtLeast95() {
        // Cropped top + High waisted bottom + elevating outerwear (3 piece) -> +18 +32 +10 = +60
        // Total should cap at 100.
        val clothing = listOf(
            ClothingItem(name = "Cropped Slim Tee", category = ClothingCategory.TOPS, colorHex = "#000000"),
            ClothingItem(name = "High Waisted Wide Leg Pants", category = ClothingCategory.BOTTOMS, colorHex = "#000000"),
            ClothingItem(name = "Jacket", category = ClothingCategory.OUTERWEAR, colorHex = "#000000")
        )
        val score = grader.grade(StyleBlueprint(selectedClothingIds = listOf("w_1", "w_2", "w_3")), clothing)
        assertThat(score).isAtLeast(95f)
    }

    @Test
    fun silhouetteGrader_slimDoesNotMatchSublimatedMuslin() {
        // "slim" should match "Slim Fit Trousers" but NOT "Sublimated Muslin Shirt"
        val clashing = listOf(
            ClothingItem(name = "Sublimated Muslin Shirt", category = ClothingCategory.TOPS, colorHex = "#000"),
            ClothingItem(name = "Wide Leg Pants", category = ClothingCategory.BOTTOMS, colorHex = "#000")
        )
        val clashScore = grader.grade(StyleBlueprint(selectedClothingIds = listOf("w_1", "w_2")), clashing)
        // Without the "slim" fitted tag on top, it does not get the tapered harmony bonus.

        val harmony = listOf(
            ClothingItem(name = "Slim Fit Shirt", category = ClothingCategory.TOPS, colorHex = "#000"),
            ClothingItem(name = "Wide Leg Pants", category = ClothingCategory.BOTTOMS, colorHex = "#000")
        )
        val harmonyScore = grader.grade(StyleBlueprint(selectedClothingIds = listOf("w_1", "w_2")), harmony)

        assertThat(harmonyScore).isGreaterThan(clashScore)
    }
}
