package com.zoewave.ashbike.features.ble

object CyclingPowerDecoder {
    data class CpsData(
        val powerWatts: Int,
        val pedalPowerBalancePercent: Double?,
        val wheelRevs: Long?,
        val lastWheelEventTimeRaw: Int?,
        val crankRevs: Int?,
        val lastCrankEventTimeRaw: Int?
    )

    fun decode(value: ByteArray): CpsData? {
        if (value.size < 4) return null
        val flags = (value[0].toInt() and 0xFF) or ((value[1].toInt() and 0xFF) shl 8)

        val powerRaw = (value[2].toInt() and 0xFF) or ((value[3].toInt() and 0xFF) shl 8)
        val powerWatts = powerRaw.toShort().toInt()

        var offset = 4

        val balancePresent = (flags and 0x0001) != 0
        var pedalPowerBalancePercent: Double? = null
        if (balancePresent) {
            if (value.size < offset + 1) return null
            pedalPowerBalancePercent = (value[offset].toInt() and 0xFF) * 0.5
            offset += 1
        }

        val torquePresent = (flags and 0x0004) != 0
        if (torquePresent) {
            if (value.size < offset + 2) return null
            offset += 2
        }

        val wheelPresent = (flags and 0x0010) != 0
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

        val crankPresent = (flags and 0x0020) != 0
        var crankRevs: Int? = null
        var lastCrankEventTimeRaw: Int? = null
        if (crankPresent) {
            if (value.size < offset + 4) return null
            crankRevs = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            lastCrankEventTimeRaw = (value[offset + 2].toInt() and 0xFF) or ((value[offset + 3].toInt() and 0xFF) shl 8)
            offset += 4
        }

        return CpsData(powerWatts, pedalPowerBalancePercent, wheelRevs, lastWheelEventTimeRaw, crankRevs, lastCrankEventTimeRaw)
    }
}
