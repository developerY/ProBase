package com.zoewave.probase.gotmind.model

import kotlinx.serialization.Serializable

@Serializable
data class Score(
    val id: Int = 0,
    val value: Int,
    val timestamp: Long = System.currentTimeMillis()
)
