package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import org.junit.Before
import org.junit.Test

class RecommendationValidatorTest {

    private lateinit var validator: RecommendationValidator

    @Before
    fun setUp() {
        validator = RecommendationValidator()
    }

    @Test
    fun validateAndSanitize_ungroundedId_stripsUngroundedItemAndFlagsError() {
        val clothingItem = ClothingItem(internalId = 1, name = "Shirt", category = ClothingCategory.TOPS, colorHex = "#000")
        val candidates = listOf(CandidateProvenance(clothingItem = clothingItem))

        val blueprint = StyleBlueprint(
            rationale = "Looks great with item w_1 and w_999.",
            selectedClothingIds = listOf("w_1", "w_999"),
            selectedCosmeticIds = emptyList(),
            recommendedPalette = emptyList()
        )

        val result = validator.validateAndSanitize(blueprint, candidates, emptyList())

        assertThat(result.isValid).isFalse()
        assertThat(result.sanitizedBlueprint.selectedClothingIds).containsExactly("w_1")
        assertThat(result.validationErrors.any { it.contains("Unknown") || it.contains("ungrounded") || it.contains("Uncertified") || it.contains("w_999") }).isTrue()
    }
}
