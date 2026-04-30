package com.zoewave.probase.gotmind.model.memblox

import kotlinx.serialization.Serializable

@Serializable
enum class MemBloxDifficulty(
    val cols: Int,
    val rows: Int,
    val targetPairs: Int,
    val spawnDelayMillis: Long,
    val label: String
) {
    EASY(6, 10, 15, 2000L, "Easy"),
    MEDIUM(9, 15, 30, 1500L, "Medium"),
    EXPERT(12, 20, 50, 1000L, "Expert")
}
