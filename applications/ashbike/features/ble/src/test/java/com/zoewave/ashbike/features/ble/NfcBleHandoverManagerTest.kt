package com.zoewave.ashbike.features.ble

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NfcBleHandoverManagerTest {

    @Test
    fun extractBleOobRecords_DirectMessage() {
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
        val record = NdefRecord.createMime("application/vnd.bluetooth.le.oob", payload)
        val message = NdefMessage(record)

        val results = NfcBleHandoverManager.extractBleOobRecords(message)

        assertEquals(1, results.size)
        val data = results[0]
        assertEquals("AA:BB:CC:DD:EE:FF", data.macAddress)
        assertEquals(BleAddressType.RANDOM, data.addressType)
        assertEquals(BleLeRole.PERIPHERAL_ONLY, data.leRole)
        assertEquals("Wahoo CAD", data.localName)
        assertTrue(data.serviceUuids.contains(CyclingBleUuids.CSC_SERVICE))
    }

    @Test
    fun extractBleOobRecords_NoOobRecords() {
        val record = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, ByteArray(0), byteArrayOf(0x04, 'e'.code.toByte(), 'x'.code.toByte(), 'a'.code.toByte(), 'm'.code.toByte(), 'p'.code.toByte(), 'l'.code.toByte(), 'e'.code.toByte(), '.'.code.toByte(), 'c'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte()))
        val message = NdefMessage(record)
        val results = NfcBleHandoverManager.extractBleOobRecords(message)
        assertTrue(results.isEmpty())
    }
}
