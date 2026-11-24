package com.zoewave.ashbike.features.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class BleOobHandoverCodecTest {

    @Test
    fun decodeOobPayload_RandomAddress_Peripheral_Name_16bitUUIDs() {
        val payload = byteArrayOf(
            0x08, 0x1B,
            0xFF.toByte(), 0xEE.toByte(), 0xDD.toByte(), 0xCC.toByte(), 0xBB.toByte(), 0xAA.toByte(),
            0x01,
            0x02, 0x1C, 0x00,
            0x0A, 0x09,
            0x57, 0x61, 0x68, 0x6F, 0x6F, 0x20, 0x43, 0x41, 0x44, // "Wahoo CAD"
            0x05, 0x03,
            0x16, 0x18, // 0x1816
            0x18, 0x18  // 0x1818
        )
        val result = BleOobHandoverCodec.decodeOobPayload(payload)
        assertTrue(result is BleOobParseResult.Valid)
        val data = (result as BleOobParseResult.Valid).data
        assertEquals("AA:BB:CC:DD:EE:FF", data.macAddress)
        assertEquals(BleAddressType.RANDOM, data.addressType)
        assertEquals(BleLeRole.PERIPHERAL_ONLY, data.leRole)
        assertEquals("Wahoo CAD", data.localName)
        assertTrue(data.serviceUuids.contains(CyclingBleUuids.CSC_SERVICE))
        assertTrue(data.serviceUuids.contains(CyclingBleUuids.CPS_SERVICE))
    }

    @Test
    fun decodeOobPayload_PublicAddress_128bitUUID() {
        val payload = byteArrayOf(
            0x08, 0x1B,
            0x06, 0x05, 0x04, 0x03, 0x02, 0x01,
            0x00,
            0x02, 0x1C, 0x00,
            0x11, 0x07,
            0x29, 0x52, 0x34, 0x72, 0x04, 0xFA.toByte(), 0xD1.toByte(), 0xB9.toByte(),
            0x9D.toByte(), 0x41, 0x48, 0xD5.toByte(), 0x80.toByte(), 0xF6.toByte(), 0x73, 0xD2.toByte()
        )
        val result = BleOobHandoverCodec.decodeOobPayload(payload)
        assertTrue(result is BleOobParseResult.Valid)
        val data = (result as BleOobParseResult.Valid).data
        assertEquals("01:02:03:04:05:06", data.macAddress)
        assertEquals(BleAddressType.PUBLIC, data.addressType)
        assertTrue(data.serviceUuids.contains(UUID.fromString("d273f680-d548-419d-b9d1-fa0472345229")))
    }

    @Test
    fun decodeOobPayload_CentralOnlyRole_Rejected() {
        val payload = byteArrayOf(
            0x08, 0x1B, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00,
            0x02, 0x1C, 0x01
        )
        val result = BleOobHandoverCodec.decodeOobPayload(payload)
        assertTrue(result is BleOobParseResult.Invalid)
    }
}
