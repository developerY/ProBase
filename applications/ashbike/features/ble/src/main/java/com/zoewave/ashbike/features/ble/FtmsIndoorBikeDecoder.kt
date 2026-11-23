package com.zoewave.ashbike.features.ble

object FtmsIndoorBikeDecoder {
    data class FtmsIndoorBikeData(
        val speedKph: Double?,
        val avgSpeedKph: Double?,
        val cadenceRpm: Double?,
        val avgCadenceRpm: Double?,
        val totalDistanceMeters: Int?,
        val resistanceLevel: Int?,
        val powerWatts: Int?,
        val avgPowerWatts: Int?,
        val heartRateBpm: Int?,
        val elapsedTimeSeconds: Int?,
        val remainingTimeSeconds: Int?
    )

    fun decode(value: ByteArray): FtmsIndoorBikeData? {
        if (value.size < 2) return null
        val flags = (value[0].toInt() and 0xFF) or ((value[1].toInt() and 0xFF) shl 8)

        var offset = 2

        // Bit 0 == 0 means Instantaneous Speed IS present
        val moreData = (flags and 0x0001) != 0
        val speedPresent = !moreData

        var speedKph: Double? = null
        if (speedPresent) {
            if (value.size < offset + 2) return null
            val rawSpeed = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            speedKph = rawSpeed.toDouble() / 100.0
            offset += 2
        }

        val avgSpeedPresent = (flags and 0x0002) != 0
        var avgSpeedKph: Double? = null
        if (avgSpeedPresent) {
            if (value.size < offset + 2) return null
            val rawAvg = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            avgSpeedKph = rawAvg.toDouble() / 100.0
            offset += 2
        }

        val cadencePresent = (flags and 0x0004) != 0
        var cadenceRpm: Double? = null
        if (cadencePresent) {
            if (value.size < offset + 2) return null
            val rawCadence = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            cadenceRpm = rawCadence.toDouble() / 2.0
            offset += 2
        }

        val avgCadencePresent = (flags and 0x0008) != 0
        var avgCadenceRpm: Double? = null
        if (avgCadencePresent) {
            if (value.size < offset + 2) return null
            val rawAvgCadence = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            avgCadenceRpm = rawAvgCadence.toDouble() / 2.0
            offset += 2
        }

        val distancePresent = (flags and 0x0010) != 0
        var totalDistanceMeters: Int? = null
        if (distancePresent) {
            if (value.size < offset + 3) return null
            totalDistanceMeters = (value[offset].toInt() and 0xFF) or
                    ((value[offset + 1].toInt() and 0xFF) shl 8) or
                    ((value[offset + 2].toInt() and 0xFF) shl 16)
            offset += 3
        }

        val resistancePresent = (flags and 0x0020) != 0
        var resistanceLevel: Int? = null
        if (resistancePresent) {
            if (value.size < offset + 2) return null
            val rawResistance = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            resistanceLevel = rawResistance.toShort().toInt()
            offset += 2
        }

        val powerPresent = (flags and 0x0040) != 0
        var powerWatts: Int? = null
        if (powerPresent) {
            if (value.size < offset + 2) return null
            val rawPower = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            powerWatts = rawPower.toShort().toInt()
            offset += 2
        }

        val avgPowerPresent = (flags and 0x0080) != 0
        var avgPowerWatts: Int? = null
        if (avgPowerPresent) {
            if (value.size < offset + 2) return null
            val rawAvgPower = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            avgPowerWatts = rawAvgPower.toShort().toInt()
            offset += 2
        }

        val energyPresent = (flags and 0x0100) != 0
        if (energyPresent) {
            if (value.size < offset + 5) return null
            // 5 bytes: uint16 Total + uint16 PerHour + uint8 PerMin. We just skip.
            offset += 5
        }

        val hrPresent = (flags and 0x0200) != 0
        var heartRateBpm: Int? = null
        if (hrPresent) {
            if (value.size < offset + 1) return null
            heartRateBpm = value[offset].toInt() and 0xFF
            offset += 1
        }

        val metPresent = (flags and 0x0400) != 0
        if (metPresent) {
            if (value.size < offset + 1) return null
            offset += 1
        }

        val elapsedTimePresent = (flags and 0x0800) != 0
        var elapsedTimeSeconds: Int? = null
        if (elapsedTimePresent) {
            if (value.size < offset + 2) return null
            elapsedTimeSeconds = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            offset += 2
        }

        val remainingTimePresent = (flags and 0x1000) != 0
        var remainingTimeSeconds: Int? = null
        if (remainingTimePresent) {
            if (value.size < offset + 2) return null
            remainingTimeSeconds = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            offset += 2
        }

        return FtmsIndoorBikeData(
            speedKph, avgSpeedKph, cadenceRpm, avgCadenceRpm, totalDistanceMeters,
            resistanceLevel, powerWatts, avgPowerWatts, heartRateBpm, elapsedTimeSeconds, remainingTimeSeconds
        )
    }
}
