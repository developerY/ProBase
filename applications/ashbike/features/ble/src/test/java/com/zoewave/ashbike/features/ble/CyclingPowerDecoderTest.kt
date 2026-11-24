package com.zoewave.ashbike.features.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CyclingPowerDecoderTest {

    @Test
    fun decode_power_only() {
        val payload = byteArrayOf(0x00, 0x00, 0xFA.toByte(), 0x00) // 250 W
        val result = CyclingPowerDecoder.decode(payload)
        assertEquals(250, result?.powerWatts)
        assertNull(result?.pedalPowerBalancePercent)
        assertNull(result?.wheelRevs)
        assertNull(result?.crankRevs)
    }

    @Test
    fun decode_power_wheel_crank() {
        // [Flags(2), Power(2), WheelRevs(4), WheelTime(2), CrankRevs(2), CrankTime(2)]
        // Flags: 0x0030 -> wheel(0x10) + crank(0x20)
        val payload = byteArrayOf(
            0x30, 0x00, // Flags
            0xD2.toByte(), 0x00, // 210 W
            // Wheel
            0x02, 0x00, 0x00, 0x00, // 2 revs
            0x00, 0x08, // 2048 ticks
            // Crank
            0x00, 0x00, // 0 revs
            0x00, 0x00  // 0 ticks
        )
        val result = CyclingPowerDecoder.decode(payload)
        assertEquals(210, result?.powerWatts)
        assertEquals(2L, result?.wheelRevs)
        assertEquals(2048, result?.lastWheelEventTimeRaw)
        assertEquals(0, result?.crankRevs)
        assertEquals(0, result?.lastCrankEventTimeRaw)
    }

    @Test
    fun decode_truncated() {
        assertNull(CyclingPowerDecoder.decode(byteArrayOf(0x30, 0x00, 0xD2.toByte()))) // missing power byte 2
        assertNull(CyclingPowerDecoder.decode(byteArrayOf(0x30, 0x00, 0xD2.toByte(), 0x00, 0x02))) // missing wheel bytes
    }
}
