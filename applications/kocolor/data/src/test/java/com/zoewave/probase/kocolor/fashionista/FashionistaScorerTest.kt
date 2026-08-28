package com.zoewave.probase.kocolor.fashionista

import com.google.common.truth.DoubleSubject
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.fashionista.color.ChromaticHarmonyEngine
import com.zoewave.probase.kocolor.fashionista.composition.CompositionEngine
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaObservation
import com.zoewave.probase.kocolor.fashionista.extraction.ColorFeatureExtractor
import com.zoewave.probase.kocolor.fashionista.extraction.CompositionFeatureExtractor
import com.zoewave.probase.kocolor.fashionista.extraction.HierarchyFeatureExtractor
import com.zoewave.probase.kocolor.fashionista.extraction.SilhouetteFeatureExtractor
import com.zoewave.probase.kocolor.fashionista.extraction.TextureFeatureExtractor
import com.zoewave.probase.kocolor.fashionista.hierarchy.VisualHierarchyEngine
import com.zoewave.probase.kocolor.fashionista.integration.CosmeticIntegrationEngine
import com.zoewave.probase.kocolor.fashionista.integration.OutfitIntegrationEngine
import com.zoewave.probase.kocolor.fashionista.scoring.CalibrationCurve
import com.zoewave.probase.kocolor.fashionista.scoring.DeterministicScorer
import com.zoewave.probase.kocolor.fashionista.scoring.FashionistaCalibration
import com.zoewave.probase.kocolor.fashionista.scoring.FashionistaScorerImpl
import com.zoewave.probase.kocolor.fashionista.scoring.InteractionModel
import com.zoewave.probase.kocolor.fashionista.silhouette.SilhouetteEngine
import com.zoewave.probase.kocolor.fashionista.silhouette.VisualMassEngine
import com.zoewave.probase.kocolor.fashionista.texture.GlcmTextureEngine
import com.zoewave.probase.kocolor.fashionista.texture.TextureHarmonyEngine
import org.junit.Before
import org.junit.Test

class FashionistaScorerTest {

    private lateinit var scorer: FashionistaScorerImpl

    @Before
    fun setup() {
        val colorEngine = ChromaticHarmonyEngine()
        val compEngine = CompositionEngine()
        val visualMassEngine = VisualMassEngine()
        val silEngine = SilhouetteEngine(visualMassEngine)
        val glcmEngine = GlcmTextureEngine()
        val texEngine = TextureHarmonyEngine(glcmEngine)
        val hierEngine = VisualHierarchyEngine()
        val cosEngine = CosmeticIntegrationEngine()
        val outfitIntEngine = OutfitIntegrationEngine(cosEngine)

        val colorExtractor = ColorFeatureExtractor(colorEngine)
        val compExtractor = CompositionFeatureExtractor(compEngine)
        val silExtractor = SilhouetteFeatureExtractor(silEngine)
        val texExtractor = TextureFeatureExtractor(texEngine)
        val hierExtractor = HierarchyFeatureExtractor(hierEngine)

        val interactionModel = InteractionModel()
        val deterministicScorer = DeterministicScorer(interactionModel)
        val calibrationCurve = CalibrationCurve()
        val calibration = FashionistaCalibration()

        scorer = FashionistaScorerImpl(
            colorExtractor,
            compExtractor,
            silExtractor,
            texExtractor,
            hierExtractor,
            outfitIntEngine,
            deterministicScorer,
            calibrationCurve,
            calibration
        )
    }

    @Test
    fun `mathematical invariant test - score in 0-100 range and coverage in 0-1 range`() {
        val observation = FashionistaObservation(
            clothingItems = listOf(
                ClothingItem(internalId = 1, name = "Silk Blouse", category = ClothingCategory.TOPS, colorHex = "#1F2937"),
                ClothingItem(internalId = 2, name = "Pleated Trousers", category = ClothingCategory.BOTTOMS, colorHex = "#B8A992"),
                ClothingItem(internalId = 3, name = "Leather Loafers", category = ClothingCategory.SHOES, colorHex = "#6B4423")
            )
        )

        val result = scorer.score(observation)

        assertThat(result.score).isAtLeast(0.0)
        assertThat(result.score).isAtMost(100.0)
        assertThat(result.coverage).isAtLeast(0.0)
        assertThat(result.coverage).isAtMost(1.0)
        assertThat(result.standardId).isEqualTo("FASHIONISTA")
        assertThat(result.standardVersion).isEqualTo(1)
    }

    @Test
    fun `zero-availability fail-safe test - empty observation returns zero score and coverage without exception`() {
        val emptyObservation = FashionistaObservation()

        val result = scorer.score(emptyObservation)

        assertThat(result.score).isEqualTo(0.0)
        assertThat(result.coverage).isEqualTo(0.0)
    }

    @Test
    fun `weighted evidence completeness test - flat-lay observation without face biometrics has lower coverage but valid score`() {
        val observationNoFace = FashionistaObservation(
            clothingItems = listOf(
                ClothingItem(internalId = 1, name = "Navy Blazer", category = ClothingCategory.OUTERWEAR, colorHex = "#1F2937"),
                ClothingItem(internalId = 2, name = "White Shirt", category = ClothingCategory.TOPS, colorHex = "#FFFFFF")
            ),
            hasBiometricData = false
        )

        val result = scorer.score(observationNoFace)

        assertThat(result.breakdown.presentationIntegration.availability).isEqualTo(0.0)
        assertThat(result.score).isGreaterThan(0.0)
        assertThat(result.coverage).isLessThan(1.0)
    }

    @Test
    fun `deterministic replicability test - identical observation produces byte-for-byte identical score`() {
        val observation = FashionistaObservation(
            clothingItems = listOf(
                ClothingItem(internalId = 10, name = "Teal Dress", category = ClothingCategory.DRESSES, colorHex = "#2F6364"),
                ClothingItem(internalId = 11, name = "Gold Heel", category = ClothingCategory.SHOES, colorHex = "#D4AF37")
            )
        )

        val score1 = scorer.score(observation)
        val score2 = scorer.score(observation)

        assertThat(score1.score).isEqualTo(score2.score)
        assertThat(score1.coverage).isEqualTo(score2.coverage)
    }
}
