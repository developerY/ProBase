package com.zoewave.probase.kocolor.features.analyzer.calibration

import com.zoewave.probase.kocolor.model.calibration.ColorSeason
import com.zoewave.probase.kocolor.model.calibration.FacialContrastVector
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorSeasonClassifierTest {

    private val classifier = ColorSeasonClassifier()

    @Test
    fun `classify returns TRUE_WINTER for cool undertone and high contrast`() {
        val vector = FacialContrastVector(
            skinLuminance = 0.8f,
            hairLuminance = 0.1f,
            eyeLuminance = 0.2f,
            contrastDelta = 0.7f
        )
        val undertone = -0.6f // Cool

        val result = classifier.classify(vector, undertone)

        assertEquals(ColorSeason.TRUE_WINTER, result)
    }

    @Test
    fun `classify returns DEEP_AUTUMN for warm undertone and high contrast`() {
        val vector = FacialContrastVector(
            skinLuminance = 0.7f,
            hairLuminance = 0.05f,
            eyeLuminance = 0.15f,
            contrastDelta = 0.65f
        )
        val undertone = 0.4f // Warm

        val result = classifier.classify(vector, undertone)

        assertEquals(ColorSeason.DEEP_AUTUMN, result)
    }

    @Test
    fun `classify returns LIGHT_SPRING for warm undertone and low contrast`() {
        val vector = FacialContrastVector(
            skinLuminance = 0.8f,
            hairLuminance = 0.6f,
            eyeLuminance = 0.7f,
            contrastDelta = 0.2f
        )
        val undertone = 0.3f // Warm

        val result = classifier.classify(vector, undertone)

        assertEquals(ColorSeason.LIGHT_SPRING, result)
    }

    @Test
    fun `classify returns SOFT_SUMMER for cool undertone and moderate contrast`() {
        val vector = FacialContrastVector(
            skinLuminance = 0.7f,
            hairLuminance = 0.3f,
            eyeLuminance = 0.4f,
            contrastDelta = 0.4f
        )
        val undertone = -0.2f // Slightly cool

        val result = classifier.classify(vector, undertone)

        assertEquals(ColorSeason.SOFT_SUMMER, result)
    }
}
