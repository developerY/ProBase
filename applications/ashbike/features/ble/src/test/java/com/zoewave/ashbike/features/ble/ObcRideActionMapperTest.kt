package com.zoewave.ashbike.features.ble

import org.junit.Assert.assertNotNull
import org.junit.Test

class ObcRideActionMapperTest {
    @Test
    fun mapperInstantiates() {
        val mapper = ObcRideActionMapper()
        assertNotNull(mapper)
    }
}
