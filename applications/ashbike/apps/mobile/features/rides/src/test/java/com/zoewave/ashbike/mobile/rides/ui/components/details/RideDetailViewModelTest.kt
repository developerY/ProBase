package com.zoewave.ashbike.mobile.rides.ui.components.details

import com.zoewave.ashbike.mobile.rides.ui.RidesEvent
import org.junit.Test
import org.junit.Assert.assertEquals

class RideDetailViewModelTest {

    @Test
    fun `rides event update ride notes instantiation`() {
        val event = RidesEvent.UpdateRideNotes("ride_456", "Great trail")
        assertEquals("ride_456", event.itemId)
        assertEquals("Great trail", event.notes)
    }
}
