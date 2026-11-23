package com.zoewave.ashbike.features.ble

object BatteryDecoder {
    fun decode(value: ByteArray): Int? {
        if (value.isEmpty()) return null
        val level = value[0].toInt() and 0xFF
        return if (level in 0..100) level else null
    }
}
