package com.zoewave.ashbike.mobile.rides.ui

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class RidesViewModelTest {

    @Test
    fun `rides event delete item instantiation`() {
        val event = RidesEvent.DeleteItem("ride_123")
        assertEquals("ride_123", event.itemId)
    }

    @Test
    fun `rides event update notes instantiation`() {
        val event = RidesEvent.UpdateRideNotes("ride_123", "Nice ride")
        assertEquals("ride_123", event.itemId)
        assertEquals("Nice ride", event.notes)
    }

    @Test
    fun `rides event sync ride instantiation`() {
        val event = RidesEvent.SyncRide("ride_123")
        assertEquals("ride_123", event.rideId)
    }
}
