package com.zoewave.probase.ashbike.wear.features.settings

import org.junit.Test
import org.junit.Assert.assertEquals

class SettingsViewModelTest {

    @Test
    fun `wear settings view model initialization defaults`() {
        val defaultState = WearSettingsUiState()
        assertEquals(0, defaultState.unsyncedRidesCount)
        assertEquals(false, defaultState.isSyncing)
    }
}
