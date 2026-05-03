package com.zoewave.probase.gotmind.model

import kotlinx.serialization.Serializable

@Serializable
enum class MemBloxEngineType {
    FALLING, STATIC
}

data class MemBloxSettings(
    val engineType: MemBloxEngineType = MemBloxEngineType.STATIC,
    val gameSpeed: Float = 1.0f,
    val dropHeight: Int = 5,
    val dropDurationMillis: Int = 3000,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true
)
