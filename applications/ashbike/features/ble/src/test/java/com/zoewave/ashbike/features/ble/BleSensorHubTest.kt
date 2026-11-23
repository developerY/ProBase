package com.zoewave.ashbike.features.ble

import org.junit.Assert.assertNotNull
import org.junit.Test

class BleSensorHubTest {
    @Test
    fun hubInstantiates() {
        val hub = BleSensorHub()
        assertNotNull(hub)
    }
}
