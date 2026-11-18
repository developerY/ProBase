package com.zoewave.probase.ashbike.mobile.ui

import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import org.junit.Test
import org.junit.Assert.assertEquals

class MainViewModelTest {

    @Test
    fun `main ui event permission result instantiation`() {
        val event = MainUiEvent.OnPermissionResult(true)
        assertEquals(true, event.isGranted)
    }

    @Test
    fun `main ui event tab selected instantiation`() {
        val event = MainUiEvent.OnTabSelected(AshBikeDestination.Trips)
        assertEquals(AshBikeDestination.Trips, event.destination)
    }
}
