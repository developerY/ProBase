package com.zoewave.ashbike.mobile.settings.ui

import com.zoewave.ashbike.model.bike.LocationEnergyLevel
import com.zoewave.probase.ashbike.database.ProfileData
import org.junit.Test
import org.junit.Assert.assertEquals

class SettingsViewModelTest {

    @Test
    fun `settings event update energy level instantiation`() {
        val event = SettingsEvent.UpdateEnergyLevel(LocationEnergyLevel.HIGH_ACCURACY)
        assertEquals(LocationEnergyLevel.HIGH_ACCURACY, event.level)
    }

    @Test
    fun `settings event update setting instantiation`() {
        val event = SettingsEvent.UpdateSetting("Theme", "Dark")
        assertEquals("Theme", event.key)
        assertEquals("Dark", event.value)
    }

    @Test
    fun `settings event save profile instantiation`() {
        val profile = ProfileData(name = "John", heightCm = "180", weightKg = "75")
        val event = SettingsEvent.SaveProfile(profile)
        assertEquals("John", event.profile.name)
    }
}
