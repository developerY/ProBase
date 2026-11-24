package com.zoewave.ashbike.features.ble

object CscDecoder {
    data class CscData(
        val wheelRevs: Long?,
        val lastWheelEventTimeRaw: Int?,
        val crankRevs: Int?,
        val lastCrankEventTimeRaw: Int?
    )

    fun decode(value: ByteArray): CscData? {
        if (value.isEmpty()) return null
        val flags = value[0].toInt() and 0xFF

        var offset = 1

        val wheelPresent = (flags and 0x01) != 0
        var wheelRevs: Long? = null
        var lastWheelEventTimeRaw: Int? = null
        if (wheelPresent) {
            if (value.size < offset + 6) return null
            wheelRevs = ((value[offset].toLong() and 0xFF) or
                    ((value[offset + 1].toLong() and 0xFF) shl 8) or
                    ((value[offset + 2].toLong() and 0xFF) shl 16) or
                    ((value[offset + 3].toLong() and 0xFF) shl 24))
            lastWheelEventTimeRaw = (value[offset + 4].toInt() and 0xFF) or ((value[offset + 5].toInt() and 0xFF) shl 8)
            offset += 6
        }

        val crankPresent = (flags and 0x02) != 0
        var crankRevs: Int? = null
        var lastCrankEventTimeRaw: Int? = null
        if (crankPresent) {
            if (value.size < offset + 4) return null
            crankRevs = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            lastCrankEventTimeRaw = (value[offset + 2].toInt() and 0xFF) or ((value[offset + 3].toInt() and 0xFF) shl 8)
            offset += 4
        }

        return CscData(wheelRevs, lastWheelEventTimeRaw, crankRevs, lastCrankEventTimeRaw)
    }
}
