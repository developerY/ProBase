package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.Formality
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class IntentFulfillmentEvaluatorTest {

    private lateinit var evaluator: IntentFulfillmentEvaluator

    @Before
    fun setUp() {
        evaluator = IntentFulfillmentEvaluator()
    }

    @Test
    fun emptyOutfit_returnsZeroWithoutFabricatingFormality() {
        val intentProfile = StyleIntentProfile(
            colorfulness = 0.9f,
            activeDimensions = setOf(IntentDimension.COLORFULNESS)
        )
        val result = evaluator.evaluate(intentProfile, emptyList(), emptyList(), emptyMap())

        assertThat(result.observedMetrics.formality).isEqualTo(0f)
        assertThat(result.score).isEqualTo(0f)
    }

    @Test
    fun activeDimensionsOnly_unrequestedContrastAndFormalityDoNotPenalizeScore() {
        val intentProfile = StyleIntentProfile(
            colorfulness = 0.85f,
            novelty = 0.90f,
            activeDimensions = setOf(IntentDimension.COLORFULNESS, IntentDimension.NOVELTY)
        )
        // High colorfulness outfit (Red)
        val clothing = listOf(
            ClothingItem(internalId = 1, name = "Red Top", category = ClothingCategory.TOPS, colorHex = "#FF0000", formality = Formality.FORMAL),
            ClothingItem(internalId = 2, name = "Red Bottom", category = ClothingCategory.BOTTOMS, colorHex = "#CC0000", formality = Formality.FORMAL)
        )

        // Low lightness contrast (reds are similar in L*), high formality (Formal = rank 5 -> 0.8), unworn novelty (1.0).
        val result = evaluator.evaluate(intentProfile, clothing, emptyList(), emptyMap())

        assertThat(result.observedMetrics.colorContrast).isLessThan(0.4f)
        assertThat(result.observedMetrics.formality).isAtLeast(0.8f)
        assertThat(result.observedMetrics.novelty).isEqualTo(1.0f)
        assertThat(result.score).isAtLeast(85f)
    }

    @Test
    fun noveltyAndFormality_reflectRotationAndRankMapping() {
        val intentProfile = StyleIntentProfile(
            novelty = 0.9f,
            formality = 0.9f,
            activeDimensions = setOf(IntentDimension.NOVELTY, IntentDimension.FORMALITY)
        )

        val unwornFormal = listOf(
            ClothingItem(internalId = 1, name = "Formal Jacket", category = ClothingCategory.OUTERWEAR, colorHex = "#000", formality = Formality.FORMAL)
        )
        val unwornResult = evaluator.evaluate(intentProfile, unwornFormal, emptyList(), emptyMap())
        assertThat(unwornResult.observedMetrics.novelty).isAtLeast(0.9f)
        assertThat(unwornResult.observedMetrics.formality).isAtLeast(0.8f)

        val wornCasual = listOf(
            ClothingItem(internalId = 2, name = "Tee", category = ClothingCategory.TOPS, colorHex = "#000", formality = Formality.CASUAL)
        )
        val rotationScores = mapOf("w_2" to 1.0)
        val wornResult = evaluator.evaluate(intentProfile, wornCasual, emptyList(), rotationScores)
        assertThat(wornResult.observedMetrics.novelty).isAtMost(0.2f)
        assertThat(wornResult.observedMetrics.formality).isAtMost(0.25f)
    }
}
