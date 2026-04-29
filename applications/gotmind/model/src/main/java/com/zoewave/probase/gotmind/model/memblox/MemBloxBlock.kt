package com.zoewave.probase.gotmind.model.memblox

import kotlinx.serialization.Serializable

@Serializable
data class MemBloxBlock(
    val id: String,
    val emoji: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false,
    val row: Int,
    val col: Int,
    val color: Int // ARGB
)
