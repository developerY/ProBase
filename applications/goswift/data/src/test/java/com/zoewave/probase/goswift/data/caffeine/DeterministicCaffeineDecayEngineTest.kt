package com.zoewave.probase.goswift.data.caffeine

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class DeterministicCaffeineDecayEngineTest {

    private val engine = DeterministicCaffeineDecayEngine()
    private val config = PharmacokineticConfig(halfLife = Duration.ofHours(5))

    @Test
    fun testHalfLifeDecay() {
        val intakeTime = Instant.parse("2023-01-01T12:00:00Z")
        val record = CaffeineIntakeRecord("1", intakeTime, 100.0, "Coffee")

        // At 0 hours
        var remaining = engine.calculateRemainingMg(listOf(record), intakeTime, config)
        assertEquals(100.0, remaining, 0.1)

        // At 5 hours (1 half-life)
        remaining = engine.calculateRemainingMg(listOf(record), intakeTime.plus(Duration.ofHours(5)), config)
        assertEquals(50.0, remaining, 0.1)

        // At 10 hours (2 half-lives)
        remaining = engine.calculateRemainingMg(listOf(record), intakeTime.plus(Duration.ofHours(10)), config)
        assertEquals(25.0, remaining, 0.1)
    }

    @Test
    fun testSuperimposedDecay() {
        val intakeTime1 = Instant.parse("2023-01-01T12:00:00Z")
        val record1 = CaffeineIntakeRecord("1", intakeTime1, 100.0, "Coffee")

        val intakeTime2 = Instant.parse("2023-01-01T17:00:00Z") // 5 hours later
        val record2 = CaffeineIntakeRecord("2", intakeTime2, 100.0, "Tea")

        val evalTime = Instant.parse("2023-01-01T17:00:00Z") // At 17:00

        // Record 1 has decayed to 50mg, Record 2 is fresh (100mg)
        val remaining = engine.calculateRemainingMg(listOf(record1, record2), evalTime, config)
        assertEquals(150.0, remaining, 0.1)
    }
}
