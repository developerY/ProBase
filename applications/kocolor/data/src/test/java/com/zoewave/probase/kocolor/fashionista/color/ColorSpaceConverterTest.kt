package com.zoewave.probase.kocolor.fashionista.color

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.kocolor.data.color.ColorHarmonyEngine
import com.zoewave.probase.kocolor.fashionista.math.CircularStatistics
import org.junit.Test

class ColorSpaceConverterTest {

    @Test
    fun goldenHexValues_blackWhiteRed_matchesExpectedLabAndChroma() {
        val black = ColorSpaceConverter.hexToLab("#000000")
        assertThat(black.l).isWithin(0.5).of(0.0)

        val white = ColorSpaceConverter.hexToLab("#FFFFFF")
        assertThat(white.l).isWithin(0.5).of(100.0)

        val redLch = ColorSpaceConverter.hexToLCh("#FF0000")
        assertThat(redLch.l).isWithin(2.0).of(53.2)
        assertThat(redLch.c).isWithin(3.0).of(104.5)
        assertThat(redLch.h).isWithin(3.0).of(40.0)
    }

    @Test
    fun circularHueBoundary_359And1_evaluatesNearZero() {
        val meanHue = CircularStatistics.chromaWeightedCircularMeanHue(doubleArrayOf(359.0, 1.0), doubleArrayOf(100.0, 100.0))
        val isNearZero = meanHue <= 1.0 || meanHue >= 359.0
        assertThat(isNearZero).isTrue()
    }

    @Test
    fun conversionConsistency_engineAndConverter_agreeWithinTolerance() {
        val engine = ColorHarmonyEngine()
        val hex = "#1E88E5"
        val engineLch = engine.hexToLCh(hex)
        val converterLch = ColorSpaceConverter.hexToLCh(hex)

        assertThat(engineLch.l.toDouble()).isWithin(1e-4).of(converterLch.l)
        assertThat(engineLch.c.toDouble()).isWithin(1e-4).of(converterLch.c)
        assertThat(engineLch.h.toDouble()).isWithin(1e-4).of(converterLch.h)
    }
}
