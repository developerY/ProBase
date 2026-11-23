package com.zoewave.ashbike.features.ble

object OpenBikeControlCodec {

    data class ObcButtonEvent(
        val buttonId: Int,
        val state: Int
    )

    fun decodeButtonState(value: ByteArray): List<ObcButtonEvent>? {
        if (value.size < 3) return null
        if (value[0].toInt() != 0x01) return null

        val events = mutableListOf<ObcButtonEvent>()
        var offset = 1
        while (offset + 1 < value.size) {
            val buttonId = value[offset].toInt() and 0xFF
            val state = value[offset + 1].toInt() and 0xFF
            events.add(ObcButtonEvent(buttonId, state))
            offset += 2
        }

        return if (events.isNotEmpty()) events else null
    }

    fun encodeHapticCommand(pattern: Int, durationMs: Int, intensity: Int): ByteArray {
        val durationUnits = (durationMs / 10).coerceIn(0, 255)
        val clampedIntensity = intensity.coerceIn(0, 255)
        return byteArrayOf(
            0x03.toByte(),
            pattern.toByte(),
            durationUnits.toByte(),
            clampedIntensity.toByte()
        )
    }

    fun encodeAppInfo(appId: String, appVersion: String, supportedButtonIds: List<Int>): ByteArray {
        val appIdBytes = appId.toByteArray(Charsets.UTF_8).take(32).toByteArray()
        val appVersionBytes = appVersion.toByteArray(Charsets.UTF_8).take(32).toByteArray()

        val size = 1 + 1 + 1 + appIdBytes.size + 1 + appVersionBytes.size + 1 + supportedButtonIds.size
        val payload = ByteArray(size)
        var offset = 0

        payload[offset++] = 0x04.toByte()
        payload[offset++] = 0x01.toByte()

        payload[offset++] = appIdBytes.size.toByte()
        System.arraycopy(appIdBytes, 0, payload, offset, appIdBytes.size)
        offset += appIdBytes.size

        payload[offset++] = appVersionBytes.size.toByte()
        System.arraycopy(appVersionBytes, 0, payload, offset, appVersionBytes.size)
        offset += appVersionBytes.size

        payload[offset++] = supportedButtonIds.size.toByte()
        for (id in supportedButtonIds) {
            payload[offset++] = id.toByte()
        }

        return payload
    }
}
