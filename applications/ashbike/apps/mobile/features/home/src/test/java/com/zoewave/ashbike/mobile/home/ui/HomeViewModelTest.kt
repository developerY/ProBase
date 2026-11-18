package com.zoewave.ashbike.mobile.home.ui

import org.junit.Test
import org.junit.Assert.assertEquals

class HomeViewModelTest {

    @Test
    fun `home event toggle demo instantiation`() {
        val event = HomeEvent.ToggleDemo
        assertEquals(HomeEvent.ToggleDemo, event)
    }

    @Test
    fun `home event toggle glass projection instantiation`() {
        val event = HomeEvent.ToggleGlassProjection
        assertEquals(HomeEvent.ToggleGlassProjection, event)
    }
}
