package com.zoewave.probase.gotmind.features.memblox

import kotlinx.serialization.Serializable

@Serializable
enum class MemBloxEngineType {
    FALLING, STATIC
}

data class MemBloxPrefs(
    val engineType: MemBloxEngineType = MemBloxEngineType.FALLING
)
