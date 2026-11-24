package com.zoewave.ashbike.features.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CscDecoderTest {

    @Test
    fun decode_wheel_only() {
        // [0x01, 0x64,0x00,0x00,0x00, 0x00,0x04] -> WheelRevs=100, Time=1024
        val payload = byteArrayOf(0x01, 0x64, 0x00, 0x00, 0x00, 0x00, 0x04)
        val result = CscDecoder.decode(payload)
        assertEquals(100L, result?.wheelRevs)
        assertEquals(1024, result?.lastWheelEventTimeRaw)
        assertNull(result?.crankRevs)
    }

    @Test
    fun decode_crank_only() {
        // [0x02, 0x32,0x00, 0x00,0x08] -> CrankRevs=50, Time=2048
        val payload = byteArrayOf(0x02, 0x32, 0x00, 0x00, 0x08)
        val result = CscDecoder.decode(payload)
        assertEquals(50, result?.crankRevs)
        assertEquals(2048, result?.lastCrankEventTimeRaw)
        assertNull(result?.wheelRevs)
    }

    @Test
    fun decode_truncated() {
        assertNull(CscDecoder.decode(byteArrayOf()))
        assertNull(CscDecoder.decode(byteArrayOf(0x01, 0x64)))
    }
}
