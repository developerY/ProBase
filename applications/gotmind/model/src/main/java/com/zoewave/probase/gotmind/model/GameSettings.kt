package com.zoewave.probase.gotmind.model

import kotlinx.serialization.Serializable

@Serializable
enum class MemBloxEngineType {
    FALLING, STATIC
}

@Serializable
enum class MindWaveMode {
    CLASSIC, SYMPHONY
}

data class GameSettings(
    val engineType: MemBloxEngineType = MemBloxEngineType.STATIC,
    val gameSpeed: Float = 1.0f,
    val dropHeight: Int = 5,
    val dropDurationMillis: Int = 3000,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val mindWaveMode: MindWaveMode = MindWaveMode.CLASSIC
)
