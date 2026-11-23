package com.zoewave.ashbike.features.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class HeartRateDecoderTest {

    @Test
    fun decode_8bit_HR() {
        val payload = byteArrayOf(0x00, 0x48) // 72 BPM
        val result = HeartRateDecoder.decode(payload)
        assertEquals(72, result?.bpm)
        assertNull(result?.contactDetected)
        assertNull(result?.energyExpendedKj)
        assertTrue(result?.rrIntervalsMs?.isEmpty() == true)
    }

    @Test
    fun decode_16bit_contact_rr() {
        // [0x17, 0x02, 0x01, 0x00, 0x04] => 16-bit HR, contact supported+detected, energy present, RR present
        // Flags (0x17 = 0001 0111) -> format16 (bit 0=1), contact (bit 1=1, bit 2=1 -> 3 -> true), energy (bit 3=0 -> false wait, bit 3 is 16? 0x17 is 16+4+2+1, so bit 0=1, bit 1=1, bit 2=1, bit 3=0, bit 4=1 (RR))
        // Let's use exact vector from prompt: [0x17, 0x02, 0x01, 0x00, 0x04]
        // 0x17 = 23 = 16 (RR) + 4 (Contact bit 2) + 2 (Contact bit 1) + 1 (16-bit HR)
        val payload = byteArrayOf(0x17, 0x02, 0x01, 0x00, 0x04)
        val result = HeartRateDecoder.decode(payload)

        // HR is 0x0102 = 258 BPM
        assertEquals(258, result?.bpm)
        assertEquals(true, result?.contactDetected)
        assertNull(result?.energyExpendedKj)
        assertEquals(1, result?.rrIntervalsMs?.size)
        // 0x0400 = 1024 -> 1024/1024 * 1000 = 1000.0 ms
        assertEquals(1000.0, result?.rrIntervalsMs?.get(0) ?: 0.0, 0.1)
    }

    @Test
    fun decode_truncated() {
        assertNull(HeartRateDecoder.decode(byteArrayOf()))
        assertNull(HeartRateDecoder.decode(byteArrayOf(0x00)))
    }
}
