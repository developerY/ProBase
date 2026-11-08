package com.zoewave.probase.kocolor.domain.taxonomy

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KoColorThresholdsTest {

    @Test
    fun thermalBands_boundaryValues_classifyCorrectly() {
        assertThat(26.1 >= KoColorThresholds.THERMAL_HOT_MIN_C).isTrue()
        assertThat(25.9 >= KoColorThresholds.THERMAL_HOT_MIN_C).isFalse()

        assertThat(20.1 >= KoColorThresholds.THERMAL_WARM_MIN_C).isTrue()
        assertThat(19.9 >= KoColorThresholds.THERMAL_WARM_MIN_C).isFalse()

        assertThat(14.1 >= KoColorThresholds.THERMAL_MILD_MIN_C).isTrue()
        assertThat(13.9 >= KoColorThresholds.THERMAL_MILD_MIN_C).isFalse()

        assertThat(7.1 >= KoColorThresholds.THERMAL_COOL_MIN_C).isTrue()
        assertThat(6.9 >= KoColorThresholds.THERMAL_COOL_MIN_C).isFalse()
    }

    @Test
    fun outerwearBanCutoff_boundaryValues_evaluates18Degrees() {
        assertThat(18.1 >= KoColorThresholds.OUTERWEAR_BAN_TEMP_C).isTrue()
        assertThat(17.9 >= KoColorThresholds.OUTERWEAR_BAN_TEMP_C).isFalse()
    }

    @Test
    fun rotationPenalty_boundaryValues_evaluatesSoftAndHardCutoffs() {
        assertThat(0.71 >= KoColorThresholds.ROTATION_HARD_BAN_PENALTY).isTrue()
        assertThat(0.69 >= KoColorThresholds.ROTATION_HARD_BAN_PENALTY).isFalse()

        assertThat(0.51 >= KoColorThresholds.ROTATION_SOFT_PENALTY).isTrue()
        assertThat(0.49 >= KoColorThresholds.ROTATION_SOFT_PENALTY).isFalse()
    }

    @Test
    fun chromaGates_boundaryValues_verifyAll5Gates() {
        assertThat(10.1f > KoColorThresholds.CHROMA_ACHROMATIC_MAX).isTrue()
        assertThat(9.9f <= KoColorThresholds.CHROMA_ACHROMATIC_MAX).isTrue()

        assertThat(15.1f >= KoColorThresholds.CHROMA_LOW_GATE).isTrue()
        assertThat(20.1f >= KoColorThresholds.CHROMA_MUTED_GATE).isTrue()
        assertThat(25.1f >= KoColorThresholds.CHROMA_STATEMENT_MIN).isTrue()
        assertThat(30.1f >= KoColorThresholds.CHROMA_HIGH_GATE).isTrue()
    }
}
