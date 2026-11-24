package com.zoewave.ashbike.features.ble

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenBikeControlCodecTest {

    @Test
    fun decodeButtonState_validMultiAction() {
        val payload = byteArrayOf(0x01, 0x01, 0x01, 0x30, 0x01, 0x14, 0x01)
        val result = OpenBikeControlCodec.decodeButtonState(payload)

        assertEquals(3, result?.size)
        assertEquals(0x01, result?.get(0)?.buttonId) // Shift Up
        assertEquals(0x01, result?.get(0)?.state) // Pressed

        assertEquals(0x30, result?.get(1)?.buttonId) // Increase Difficulty
        assertEquals(0x01, result?.get(1)?.state)

        assertEquals(0x14, result?.get(2)?.buttonId) // Select
        assertEquals(0x01, result?.get(2)?.state)
    }

    @Test
    fun decodeButtonState_analogBrake() {
        // 100% Brake (0x65 = 101, but spec mapping says 0x65 = 100%)
        val payloadFull = byteArrayOf(0x01, 0x1A, 0x65)
        val resultFull = OpenBikeControlCodec.decodeButtonState(payloadFull)
        assertEquals(1, resultFull?.size)
        assertEquals(0x1A, resultFull?.get(0)?.buttonId)
        assertEquals(101, resultFull?.get(0)?.state)

        val payloadRelease = byteArrayOf(0x01, 0x1A, 0x00)
        val resultRelease = OpenBikeControlCodec.decodeButtonState(payloadRelease)
        assertEquals(0, resultRelease?.get(0)?.state)
    }

    @Test
    fun decodeButtonState_invalid() {
        assertNull(OpenBikeControlCodec.decodeButtonState(byteArrayOf(0x01, 0x01)))
        assertNull(OpenBikeControlCodec.decodeButtonState(byteArrayOf(0x02, 0x01, 0x01))) // Wrong message type
        assertNull(OpenBikeControlCodec.decodeButtonState(byteArrayOf()))
    }

    @Test
    fun encodeHapticCommand() {
        val result = OpenBikeControlCodec.encodeHapticCommand(1, 150, 200)
        // [0x03, pattern=1, duration=15, intensity=200]
        assertArrayEquals(byteArrayOf(0x03, 0x01, 0x0F, 0xC8.toByte()), result)
    }

    @Test
    fun encodeAppInfo() {
        val result = OpenBikeControlCodec.encodeAppInfo("AshBike", "1.1.0", listOf(0x01, 0x02, 0x33, 0x34))
        // 0x04, 0x01, len=7, A,s,h,B,i,k,e, len=5, 1,.,1,.,0, num_buttons=4, 0x01, 0x02, 0x33, 0x34
        val expected = byteArrayOf(
            0x04, 0x01,
            0x07, 'A'.code.toByte(), 's'.code.toByte(), 'h'.code.toByte(), 'B'.code.toByte(), 'i'.code.toByte(), 'k'.code.toByte(), 'e'.code.toByte(),
            0x05, '1'.code.toByte(), '.'.code.toByte(), '1'.code.toByte(), '.'.code.toByte(), '0'.code.toByte(),
            0x04, 0x01, 0x02, 0x33, 0x34
        )
        assertArrayEquals(expected, result)
    }
}
