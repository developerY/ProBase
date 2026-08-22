package com.zoewave.probase.kocolor.features.analyzer.calibration

import android.hardware.SensorManager
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LightingValidatorTest {

    private val sensorManager: SensorManager = mockk(relaxed = true)
    private val validator = LightingValidator(sensorManager)

    @Test
    fun `isLightingOptimal returns true for lux in range 300 to 10000`() {
        assertTrue(validator.isLightingOptimal(300f))
        assertTrue(validator.isLightingOptimal(5000f))
        assertTrue(validator.isLightingOptimal(10000f))
    }

    @Test
    fun `isLightingOptimal returns false for lux out of range`() {
        assertFalse(validator.isLightingOptimal(299f))
        assertFalse(validator.isLightingOptimal(100f))
        assertFalse(validator.isLightingOptimal(10001f))
        assertFalse(validator.isLightingOptimal(20000f))
    }
}
