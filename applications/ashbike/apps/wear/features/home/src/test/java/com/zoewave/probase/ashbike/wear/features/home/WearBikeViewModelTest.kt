package com.zoewave.probase.ashbike.wear.features.home

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

class WearBikeViewModelTest {

    @Test
    fun `wear bike ui state default initialization`() {
        val state = WearBikeUiState()
        assertEquals(0f, state.currentSpeed, 0.001f)
        assertEquals(0f, state.distanceMeters, 0.001f)
        assertEquals(0, state.heartRate)
        assertFalse(state.isTracking)
    }
}
