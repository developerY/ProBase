package com.zoewave.probase.feature.places.ui

import org.junit.Test
import org.junit.Assert.assertEquals

class CoffeeShopViewModelTest {

    @Test
    fun `find cafes in area event instantiation`() {
        val event = CoffeeShopEvent.FindCafesInArea(
            latitude = 40.7128,
            longitude = -74.0060,
            radius = 500.0
        )
        assertEquals(40.7128, event.latitude, 0.0001)
        assertEquals(-74.0060, event.longitude, 0.0001)
        assertEquals(500.0, event.radius, 0.0001)
    }
}
