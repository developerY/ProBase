package com.zoewave.probase.ashbike.wear.features.rides

import org.junit.Test
import org.junit.Assert.assertEquals

class WearRidesViewModelTest {

    @Test
    fun `wear rides event delete click instantiation`() {
        val event = RidesEvent.OnDeleteClick("wear_123")
        assertEquals("wear_123", event.rideId)
    }
}
