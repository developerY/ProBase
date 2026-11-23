package com.zoewave.ashbike.features.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FtmsIndoorBikeDecoderTest {

    @Test
    fun decode_standardTrainerPacket_speedPresent() {
        // [0x44, 0x00, 0xB2, 0x0C, 0xB5, 0x00, 0xF0, 0x00]
        // Flags: 0x0044 -> Bit 2 (Cadence 0x04) + Bit 6 (Power 0x40). Bit 0 is 0 -> Speed IS present.
        val payload = byteArrayOf(0x44, 0x00, 0xB2.toByte(), 0x0C, 0xB5.toByte(), 0x00, 0xF0.toByte(), 0x00)
        val result = FtmsIndoorBikeDecoder.decode(payload)

        // Speed: 0x0CB2 = 3250 -> 32.50 km/h
        assertEquals(32.50, result?.speedKph!!, 1e-3)

        // Cadence: 0x00B5 = 181 -> 181 / 2.0 = 90.5 RPM
        assertEquals(90.5, result?.cadenceRpm!!, 1e-3)

        // Power: 0x00F0 = 240 W
        assertEquals(240, result?.powerWatts)

        assertNull(result?.totalDistanceMeters)
    }

    @Test
    fun decode_invertedBit0_speedOmitted() {
        // [0x71, 0x02, 0xA0, 0x86, 0x01, 0x0C, 0x00, 0xB9, 0x00, 0x94.toByte()]
        // Flags: 0x0271 = 1 (More Data) + 16 (Distance) + 32 (Resistance) + 64 (Power) + 512 (HR)
        // More Data (Bit 0) = 1 -> Speed OMITTED
        val payload = byteArrayOf(
            0x71, 0x02,
            0xA0.toByte(), 0x86.toByte(), 0x01, // Distance 0x0186A0 = 100,000 meters
            0x0C, 0x00, // Resistance 12
            0xB9.toByte(), 0x00, // Power 185
            0x94.toByte() // HR 148
        )
        val result = FtmsIndoorBikeDecoder.decode(payload)

        assertNull(result?.speedKph)
        assertEquals(100_000, result?.totalDistanceMeters)
        assertEquals(12, result?.resistanceLevel)
        assertEquals(185, result?.powerWatts)
        assertEquals(148, result?.heartRateBpm)
    }

    @Test
    fun decode_truncated() {
        assertNull(FtmsIndoorBikeDecoder.decode(byteArrayOf(0x44, 0x00, 0xB2.toByte(), 0x0C)))
        assertNull(FtmsIndoorBikeDecoder.decode(byteArrayOf(0x00)))
        assertNull(FtmsIndoorBikeDecoder.decode(byteArrayOf()))
    }
}
