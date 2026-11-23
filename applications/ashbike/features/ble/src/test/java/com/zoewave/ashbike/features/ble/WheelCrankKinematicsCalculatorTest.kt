package com.zoewave.ashbike.features.ble

import org.junit.Assert.assertEquals
import org.junit.Test

class WheelCrankKinematicsCalculatorTest {

    @Test
    fun updateWheel_calculatesSpeedCorrectly() {
        var currentTime = 0L
        val calc = WheelCrankKinematicsCalculator(
            wheelTimeResolutionHz = 1024.0,
            clockMs = { currentTime },
            wheelCircumferenceMm = 2105
        )

        // Baseline [0x01, 0x64,0x00,0x00,0x00, 0x00,0x04] -> WheelRevs=100, Time=1024
        calc.updateWheel(100L, 1024)
        assertEquals(0.0, calc.deltaDistanceMeters, 1e-3)
        assertEquals(0.0, calc.speedKph, 1e-3)

        // Next [0x01, 0x66,0x00,0x00,0x00, 0x00,0x08] -> WheelRevs=102, Time=2048 (1 second diff)
        currentTime = 1000L
        calc.updateWheel(102L, 2048)

        // 2 revs * 2105 mm = 4210 mm = 4.21 m
        assertEquals(4.21, calc.deltaDistanceMeters, 1e-3)
        // 4.21 m / 1 s = 4.21 m/s -> 15.156 km/h
        assertEquals(15.156, calc.speedKph, 1e-3)
    }

    @Test
    fun updateCrank_calculatesCadenceCorrectly() {
        var currentTime = 0L
        val calc = WheelCrankKinematicsCalculator(
            wheelTimeResolutionHz = 1024.0,
            clockMs = { currentTime }
        )

        // Baseline 50 revs / 2048 ticks (2 sec)
        calc.updateCrank(50, 2048)
        assertEquals(0.0, calc.cadenceRpm, 1e-3)

        // Next 53 revs (+3) / 4096 ticks (2 seconds later)
        currentTime = 2000L
        calc.updateCrank(53, 4096)

        // 3 revs / 2 seconds = 1.5 revs/s * 60 = 90 RPM
        assertEquals(90.0, calc.cadenceRpm, 1e-3)
    }

    @Test
    fun updateWheel_handlesRollover() {
        var currentTime = 0L
        val calc = WheelCrankKinematicsCalculator(
            wheelTimeResolutionHz = 1024.0,
            clockMs = { currentTime },
            wheelCircumferenceMm = 2105
        )

        calc.updateWheel(0xFFFFFFFE, 1024)
        currentTime = 1000L
        calc.updateWheel(1, 2048) // Rollover: 0xFFFFFFFE -> 0xFFFFFFFF -> 0 -> 1 = 3 revs

        // 3 revs * 2.105m = 6.315 m over 1s = 6.315 m/s = 22.734 km/h
        assertEquals(6.315, calc.deltaDistanceMeters, 1e-3)
        assertEquals(22.734, calc.speedKph, 1e-3)
    }
}
