package com.zoewave.ashbike.features.ble

import java.util.UUID

const val MIME_TYPE_BLE_OOB = "application/vnd.bluetooth.le.oob"

enum class BleAddressType(val wireValue: Int) {
    PUBLIC(0x00), RANDOM(0x01)
}

enum class BleLeRole(val wireValue: Int) {
    PERIPHERAL_ONLY(0x00),
    CENTRAL_ONLY(0x01),
    BOTH_PERIPHERAL_PREFERRED(0x02),
    BOTH_CENTRAL_PREFERRED(0x03)
}

data class BleOobHandoverData(
    val macAddress: String,
    val addressType: BleAddressType,
    val leRole: BleLeRole = BleLeRole.PERIPHERAL_ONLY,
    val localName: String? = null,
    val serviceUuids: List<UUID> = emptyList(),
    val appearance: Int? = null,
    val smTkValue: ByteArray? = null,
    val lescConfirmation: ByteArray? = null,
    val lescRandom: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleOobHandoverData

        if (macAddress != other.macAddress) return false
        if (addressType != other.addressType) return false
        if (leRole != other.leRole) return false
        if (localName != other.localName) return false
        if (serviceUuids != other.serviceUuids) return false
        if (appearance != other.appearance) return false
        if (smTkValue != null) {
            if (other.smTkValue == null) return false
            if (!smTkValue.contentEquals(other.smTkValue)) return false
        } else if (other.smTkValue != null) return false
        if (lescConfirmation != null) {
            if (other.lescConfirmation == null) return false
            if (!lescConfirmation.contentEquals(other.lescConfirmation)) return false
        } else if (other.lescConfirmation != null) return false
        if (lescRandom != null) {
            if (other.lescRandom == null) return false
            if (!lescRandom.contentEquals(other.lescRandom)) return false
        } else if (other.lescRandom != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = macAddress.hashCode()
        result = 31 * result + addressType.hashCode()
        result = 31 * result + leRole.hashCode()
        result = 31 * result + (localName?.hashCode() ?: 0)
        result = 31 * result + serviceUuids.hashCode()
        result = 31 * result + (appearance ?: 0)
        result = 31 * result + (smTkValue?.contentHashCode() ?: 0)
        result = 31 * result + (lescConfirmation?.contentHashCode() ?: 0)
        result = 31 * result + (lescRandom?.contentHashCode() ?: 0)
        return result
    }
}

sealed interface BleOobParseResult {
    data class Valid(val data: BleOobHandoverData) : BleOobParseResult
    data class Invalid(val reason: String) : BleOobParseResult
}

object BleOobHandoverCodec {

    fun decodeOobPayload(payload: ByteArray): BleOobParseResult {
        if (payload.isEmpty()) return BleOobParseResult.Invalid("Payload is empty")

        var offset = 0
        var macAddress: String? = null
        var addressType: BleAddressType? = null
        var leRole = BleLeRole.PERIPHERAL_ONLY
        var localName: String? = null
        val serviceUuids = mutableListOf<UUID>()
        var appearance: Int? = null
        var smTkValue: ByteArray? = null
        var lescConfirmation: ByteArray? = null
        var lescRandom: ByteArray? = null

        while (offset < payload.size) {
            val length = payload[offset].toInt() and 0xFF
            if (length == 0 || offset + 1 + length > payload.size) {
                break // Safe truncation skip
            }

            val adType = payload[offset + 1].toInt() and 0xFF
            val dataOffset = offset + 2
            val dataLength = length - 1

            when (adType) {
                0x1B -> { // LE Bluetooth Device Address
                    if (dataLength >= 7) {
                        val macBytes = payload.copyOfRange(dataOffset, dataOffset + 6)
                        macAddress = macBytes.reversedArray().joinToString(":") { "%02X".format(it) }

                        val typeByte = payload[dataOffset + 6].toInt() and 0xFF
                        addressType = BleAddressType.entries.find { it.wireValue == typeByte }
                        if (addressType == null) {
                            return BleOobParseResult.Invalid("Invalid address type: $typeByte")
                        }
                    }
                }
                0x1C -> { // LE Role
                    if (dataLength >= 1) {
                        val roleByte = payload[dataOffset].toInt() and 0xFF
                        val role = BleLeRole.entries.find { it.wireValue == roleByte }
                        if (role == BleLeRole.CENTRAL_ONLY) {
                            return BleOobParseResult.Invalid("CENTRAL_ONLY role is not supported")
                        }
                        if (role != null) leRole = role
                    }
                }
                0x08, 0x09 -> { // Shortened / Complete Local Name
                    if (dataLength > 0) {
                        localName = String(payload, dataOffset, dataLength, Charsets.UTF_8)
                    }
                }
                0x02, 0x03 -> { // 16-bit UUIDs
                    var i = 0
                    while (i + 1 < dataLength) {
                        val uuid16 = (payload[dataOffset + i].toInt() and 0xFF) or ((payload[dataOffset + i + 1].toInt() and 0xFF) shl 8)
                        val fullUuid = String.format("0000%04X-0000-1000-8000-00805F9B34FB", uuid16)
                        serviceUuids.add(UUID.fromString(fullUuid))
                        i += 2
                    }
                }
                0x06, 0x07 -> { // 128-bit UUIDs
                    var i = 0
                    while (i + 15 < dataLength) {
                        val uuidBytes = payload.copyOfRange(dataOffset + i, dataOffset + i + 16).reversedArray()
                        val msb = uuidBytes.copyOfRange(0, 8).fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFFL) }
                        val lsb = uuidBytes.copyOfRange(8, 16).fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFFL) }
                        serviceUuids.add(UUID(msb, lsb))
                        i += 16
                    }
                }
                0x19 -> { // Appearance
                    if (dataLength >= 2) {
                        appearance = (payload[dataOffset].toInt() and 0xFF) or ((payload[dataOffset + 1].toInt() and 0xFF) shl 8)
                    }
                }
                0x10 -> { // SM TK Value
                    if (dataLength == 16) smTkValue = payload.copyOfRange(dataOffset, dataOffset + 16)
                }
                0x22 -> { // LESC Confirmation
                    if (dataLength == 16) lescConfirmation = payload.copyOfRange(dataOffset, dataOffset + 16)
                }
                0x23 -> { // LESC Random
                    if (dataLength == 16) lescRandom = payload.copyOfRange(dataOffset, dataOffset + 16)
                }
                // Skip unknown AD types
            }
            offset += 1 + length
        }

        if (macAddress == null || addressType == null) {
            return BleOobParseResult.Invalid("Missing mandatory MAC address or Address Type")
        }

        return BleOobParseResult.Valid(BleOobHandoverData(macAddress, addressType, leRole, localName, serviceUuids, appearance, smTkValue, lescConfirmation, lescRandom))
    }

    fun encodeOobPayload(data: BleOobHandoverData): ByteArray {
        val payload = mutableListOf<Byte>()

        // 1. Mandatory MAC Address (0x1B) -> length 8, adType 0x1B, 6 bytes MAC (LE), 1 byte type
        payload.add(8)
        payload.add(0x1B)
        val macParts = data.macAddress.split(":")
        for (i in 5 downTo 0) {
            payload.add(macParts[i].toInt(16).toByte())
        }
        payload.add(data.addressType.wireValue.toByte())

        // 2. LE Role (0x1C) -> length 2, adType 0x1C, 1 byte role
        payload.add(2)
        payload.add(0x1C)
        payload.add(data.leRole.wireValue.toByte())

        // 3. Name (0x09)
        if (data.localName != null) {
            val nameBytes = data.localName.toByteArray(Charsets.UTF_8)
            payload.add((nameBytes.size + 1).toByte())
            payload.add(0x09)
            payload.addAll(nameBytes.toList())
        }

        // 4. UUIDs
        val uuid16s = data.serviceUuids.filter { it.toString().startsWith("0000") && it.toString().endsWith("-0000-1000-8000-00805f9b34fb") }
        val uuid128s = data.serviceUuids - uuid16s.toSet()

        if (uuid16s.isNotEmpty()) {
            payload.add((1 + uuid16s.size * 2).toByte())
            payload.add(0x03)
            for (uuid in uuid16s) {
                val uuidStr = uuid.toString().substring(4, 8)
                val value = uuidStr.toInt(16)
                payload.add((value and 0xFF).toByte())
                payload.add(((value shr 8) and 0xFF).toByte())
            }
        }

        if (uuid128s.isNotEmpty()) {
            payload.add((1 + uuid128s.size * 16).toByte())
            payload.add(0x07)
            for (uuid in uuid128s) {
                var msb = uuid.mostSignificantBits
                var lsb = uuid.leastSignificantBits
                val bytes = ByteArray(16)
                for (i in 15 downTo 8) {
                    bytes[i] = (lsb and 0xFF).toByte()
                    lsb = lsb shr 8
                }
                for (i in 7 downTo 0) {
                    bytes[i] = (msb and 0xFF).toByte()
                    msb = msb shr 8
                }
                payload.addAll(bytes.reversedArray().toList())
            }
        }

        return payload.toByteArray()
    }
}
