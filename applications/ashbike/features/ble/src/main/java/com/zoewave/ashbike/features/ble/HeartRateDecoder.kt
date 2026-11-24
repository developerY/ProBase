package com.zoewave.ashbike.features.ble

object HeartRateDecoder {
    data class HrData(
        val bpm: Int,
        val contactDetected: Boolean?,
        val energyExpendedKj: Int?,
        val rrIntervalsMs: List<Double>
    )

    fun decode(value: ByteArray): HrData? {
        if (value.isEmpty()) return null
        val flags = value[0].toInt()
        val format16Bit = (flags and 0x01) != 0
        val contactStatus = (flags and 0x06) shr 1
        val energyPresent = (flags and 0x08) != 0
        val rrPresent = (flags and 0x10) != 0

        var offset = 1
        val bpm: Int
        if (format16Bit) {
            if (value.size < offset + 2) return null
            bpm = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            offset += 2
        } else {
            if (value.size < offset + 1) return null
            bpm = value[offset].toInt() and 0xFF
            offset += 1
        }

        val contactDetected = when (contactStatus) {
            2 -> false
            3 -> true
            else -> null
        }

        val energyExpendedKj: Int?
        if (energyPresent) {
            if (value.size < offset + 2) return null
            energyExpendedKj = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            offset += 2
        } else {
            energyExpendedKj = null
        }

        val rrIntervals = mutableListOf<Double>()
        if (rrPresent) {
            while (offset + 2 <= value.size) {
                val raw = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
                rrIntervals.add((raw.toDouble() / 1024.0) * 1000.0)
                offset += 2
            }
        }

        return HrData(bpm, contactDetected, energyExpendedKj, rrIntervals)
    }
}
