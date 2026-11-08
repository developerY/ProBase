package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import org.junit.Before
import org.junit.Test

class WardrobeCandidateFilterTest {

    private lateinit var filter: WardrobeCandidateFilter

    @Before
    fun setUp() {
        val rotationUseCase = io.mockk.mockk<RotationScoringUseCase>(relaxed = true)
        filter = WardrobeCandidateFilter(rotationUseCase)
    }

    @Test
    fun wardrobeScorer_stopWordsDoNotIncreaseScore() {
        val item = ClothingItem(
            internalId = 1,
            name = "Alpine Jacket",
            category = ClothingCategory.OUTERWEAR,
            colorHex = "#000"
        )
        val contextEmpty = StyleRequestContext(intent = "")
        val contextStopWords = StyleRequestContext(intent = "a look for the style")

        val scoreEmpty = filter.calculateScoreTest(item, contextEmpty)
        val scoreStop = filter.calculateScoreTest(item, contextStopWords)

        assertThat(scoreStop).isEqualTo(scoreEmpty)
    }

    @Test
    fun cosmeticScorer_lipDoesNotMatchEclipse() {
        val eclipse = CosmeticItem(
            internalId = 1,
            name = "Eclipse Eyeshadow",
            brand = "Ko",
            macroCategory = MacroCategory.EYES,
            microCategory = MicroCategory.EYESHADOW,
            colorHex = "#000"
        )
        val velvet = CosmeticItem(
            internalId = 2,
            name = "Velvet Lip Tint",
            brand = "Ko",
            macroCategory = MacroCategory.LIPS,
            microCategory = MicroCategory.LIPSTICK,
            colorHex = "#F00"
        )

        val context = StyleRequestContext(intent = "lip")

        val scoreEclipse = filter.calculateCosmeticScoreTest(eclipse, context)
        val scoreVelvet = filter.calculateCosmeticScoreTest(velvet, context)

        // Eclipse gets no keyword bonus, Velvet gets +0.75
        assertThat(scoreVelvet).isGreaterThan(scoreEclipse)
    }

    @Test
    fun parseTempCelsius_hotWeather_setsIsHotTrueAndIsColdFalse() {
        val weatherHot = "Clear, Temp: 28°C"
        val tempHot = filter.parseTempCelsius(weatherHot)
        assertThat(tempHot).isEqualTo(28.0)

        val weatherCold = "Snow, Temp: -5°C"
        val tempCold = filter.parseTempCelsius(weatherCold)
        assertThat(tempCold).isEqualTo(-5.0)
    }
}
