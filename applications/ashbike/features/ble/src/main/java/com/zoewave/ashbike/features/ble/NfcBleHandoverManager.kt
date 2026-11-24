package com.zoewave.ashbike.features.ble

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

enum class NfcAvailability { UNAVAILABLE, DISABLED, READY }

sealed interface NfcWriteResult {
    data object Success : NfcWriteResult
    data object ReadOnlyTag : NfcWriteResult
    data object TagTooSmall : NfcWriteResult
    data class IoError(val error: String) : NfcWriteResult
}

object NfcBleHandoverManager {

    fun checkAvailability(activity: Activity): NfcAvailability {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        return when {
            adapter == null -> NfcAvailability.UNAVAILABLE
            !adapter.isEnabled -> NfcAvailability.DISABLED
            else -> NfcAvailability.READY
        }
    }

    fun extractBleOobRecords(message: NdefMessage?): List<BleOobHandoverData> {
        val results = mutableListOf<BleOobHandoverData>()
        if (message == null) return results

        for (record in message.records) {
            val tnf = record.tnf
            val type = String(record.type, Charsets.US_ASCII)

            if (tnf == NdefRecord.TNF_MIME_MEDIA && type == MIME_TYPE_BLE_OOB) {
                val parsed = BleOobHandoverCodec.decodeOobPayload(record.payload)
                if (parsed is BleOobParseResult.Valid) {
                    results.add(parsed.data)
                }
            }
        }
        return results
    }

    fun enableReadMode(activity: Activity, onResult: (List<BleOobHandoverData>) -> Unit) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V

        adapter.enableReaderMode(activity, { tag ->
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                try {
                    ndef.connect()
                    val msg = ndef.ndefMessage
                    val records = extractBleOobRecords(msg)
                    onResult(records)
                } catch (e: IOException) {
                    // Ignore transient errors
                } finally {
                    try { ndef.close() } catch (e: IOException) {}
                }
            }
        }, flags, null)
    }

    fun enableWriteMode(activity: Activity, data: BleOobHandoverData, onWriteResult: (NfcWriteResult) -> Unit) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V

        adapter.enableReaderMode(activity, { tag ->
            CoroutineScope(Dispatchers.IO).launch {
                val payload = BleOobHandoverCodec.encodeOobPayload(data)
                val record = NdefRecord.createMime(MIME_TYPE_BLE_OOB, payload)
                val message = NdefMessage(arrayOf(record))

                val result = writeTag(tag, message)
                onWriteResult(result)
            }
        }, flags, null)
    }

    private fun writeTag(tag: Tag, message: NdefMessage): NfcWriteResult {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                if (!ndef.isWritable) {
                    return NfcWriteResult.ReadOnlyTag
                }
                if (ndef.maxSize < message.toByteArray().size) {
                    return NfcWriteResult.TagTooSmall
                }
                ndef.writeNdefMessage(message)
                return NfcWriteResult.Success
            } catch (e: IOException) {
                return NfcWriteResult.IoError(e.message ?: "Unknown IO Error")
            } catch (e: android.nfc.FormatException) {
                return NfcWriteResult.IoError("Format Error")
            } finally {
                try { ndef.close() } catch (e: IOException) {}
            }
        } else {
            val formatable = NdefFormatable.get(tag)
            if (formatable != null) {
                try {
                    formatable.connect()
                    formatable.format(message)
                    return NfcWriteResult.Success
                } catch (e: IOException) {
                    return NfcWriteResult.IoError(e.message ?: "Unknown IO Error")
                } catch (e: android.nfc.FormatException) {
                    return NfcWriteResult.IoError("Format Error")
                } finally {
                    try { formatable.close() } catch (e: IOException) {}
                }
            }
        }
        return NfcWriteResult.IoError("Tag does not support NDEF")
    }

    fun disableReaderMode(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        adapter?.disableReaderMode(activity)
    }
}
