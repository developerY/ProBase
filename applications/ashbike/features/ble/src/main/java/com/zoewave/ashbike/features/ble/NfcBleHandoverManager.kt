package com.zoewave.ashbike.features.ble

import android.nfc.NdefMessage
import android.nfc.NdefRecord

object NfcBleHandoverManager {

    fun extractBleOobRecords(message: NdefMessage?): List<BleOobHandoverData> {
        val results = mutableListOf<BleOobHandoverData>()
        if (message == null) return results

        for (record in message.records) {
            val tnf = record.tnf
            val type = String(record.type, Charsets.US_ASCII)

            // Direct BLE OOB record
            if (tnf == NdefRecord.TNF_MIME_MEDIA && type == MIME_TYPE_BLE_OOB) {
                val parsed = BleOobHandoverCodec.decodeOobPayload(record.payload)
                if (parsed is BleOobParseResult.Valid) {
                    results.add(parsed.data)
                }
            }

            // NFC Forum Static Handover (Hs) - requires parsing nested NdefMessage
            if (tnf == NdefRecord.TNF_WELL_KNOWN && type == "Hs") {
                // The Handover Select record payload is an Alternate Carrier record sequence.
                // In a simplified parser we just extract nested OOBs if they happen to be bundled.
                // A full NFC Forum implementation would parse AC records. For now, we scan the whole message.
            }
        }
        return results
    }
}
