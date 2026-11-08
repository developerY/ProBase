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
        io.mockk.mockkStatic(android.util.Log::class)
        io.mockk.every { android.util.Log.d(any(), any()) } returns 0
        io.mockk.every { android.util.Log.w(any(), any<String>()) } returns 0
        io.mockk.every { android.util.Log.e(any(), any<String>()) } returns 0
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

    @Test
    fun validateAndSanitize_prepCosmetic_stripsPrepItem() {
        val prepCosmetic = com.zoewave.probase.core.model.ritual.CosmeticItem(
            internalId = 10,
            name = "Primer",
            brand = "Ko",
            macroCategory = com.zoewave.probase.core.model.ritual.MacroCategory.PREP,
            microCategory = com.zoewave.probase.core.model.ritual.MicroCategory.PRIMER,
            colorHex = "#FFF"
        )
        val lipCosmetic = com.zoewave.probase.core.model.ritual.CosmeticItem(
            internalId = 20,
            name = "Lipstick",
            brand = "Ko",
            macroCategory = com.zoewave.probase.core.model.ritual.MacroCategory.LIPS,
            microCategory = com.zoewave.probase.core.model.ritual.MicroCategory.LIPSTICK,
            colorHex = "#F00"
        )
        val cosmetics = listOf(
            CandidateProvenance(cosmeticItem = prepCosmetic),
            CandidateProvenance(cosmeticItem = lipCosmetic)
        )

        val blueprint = StyleBlueprint(
            rationale = "Beautiful look.",
            selectedClothingIds = emptyList(),
            selectedCosmeticIds = listOf("c_10", "c_20"),
            recommendedPalette = emptyList()
        )

        val result = validator.validateAndSanitize(blueprint, emptyList(), cosmetics)

        assertThat(result.sanitizedBlueprint.selectedCosmeticIds).containsExactly("c_20")
        assertThat(result.isValid).isFalse()
    }

    @Test
    fun validateAndSanitize_rationaleSentenceNamingUnselectedItem_filtersSentencePreservingDecimals() {
        val top1 = ClothingItem(internalId = 1, name = "Silk Top", category = ClothingCategory.TOPS, colorHex = "#000")
        val top2 = ClothingItem(internalId = 2, name = "Wool Jacket", category = ClothingCategory.TOPS, colorHex = "#000")
        val candidates = listOf(CandidateProvenance(clothingItem = top1), CandidateProvenance(clothingItem = top2))

        val blueprint = StyleBlueprint(
            rationale = "The Silk Top gives a 4.00 rating. The Wool Jacket was not selected.",
            selectedClothingIds = listOf("w_1"),
            selectedCosmeticIds = emptyList(),
            recommendedPalette = emptyList()
        )

        val result = validator.validateAndSanitize(blueprint, candidates, emptyList())

        assertThat(result.sanitizedBlueprint.rationale).contains("Silk Top")
        assertThat(result.sanitizedBlueprint.rationale).contains("4.00")
        assertThat(result.sanitizedBlueprint.rationale).doesNotContain("Wool Jacket")
    }
}
