package com.zoewave.probase.kocolor.model.playlist

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ProjectedRotationStateTest {

    @Test
    fun `simulateWear increments count and updates timestamp`() {
        val initialHistory = mapOf(
            "p1" to UsageSnapshot(useCount = 2, lastUsedAt = 1000L)
        )
        val state = ProjectedRotationState(initialHistory)
        val simulatedTime = Instant.ofEpochMilli(2000L)

        state.simulateWear("p1", simulatedTime)

        val updated = state.getUsage("p1")
        assertEquals(3, updated?.useCount)
        assertEquals(2000L, updated?.lastUsedAt)
    }

    @Test
    fun `simulateWear handles new product`() {
        val state = ProjectedRotationState(emptyMap())
        val simulatedTime = Instant.ofEpochMilli(5000L)

        state.simulateWear("new_p", simulatedTime)

        val usage = state.getUsage("new_p")
        assertEquals(1, usage?.useCount)
        assertEquals(5000L, usage?.lastUsedAt)
    }
}
