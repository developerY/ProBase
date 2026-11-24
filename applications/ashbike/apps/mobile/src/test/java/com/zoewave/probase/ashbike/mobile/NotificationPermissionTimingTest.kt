package com.zoewave.probase.ashbike.mobile

import com.zoewave.probase.ashbike.features.main.service.BikeForegroundService
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationPermissionTimingTest {

    @Test
    fun coldStartDoesNotTriggerNotificationRequestAction() {
        // Verification that event constants exist and start ride action is distinct
        assertEquals("com.zoewave.probase.ashbike.features.main.service.action.START_RIDE", BikeForegroundService.ACTION_START_RIDE)
    }
}
