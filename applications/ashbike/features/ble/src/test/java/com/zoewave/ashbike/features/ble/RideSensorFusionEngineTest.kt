package com.zoewave.ashbike.features.ble

import org.junit.Assert.assertNotNull
import org.junit.Test

class RideSensorFusionEngineTest {
    @Test
    fun engineInstantiates() {
        val engine = RideSensorFusionEngine()
        assertNotNull(engine)
    }
}
