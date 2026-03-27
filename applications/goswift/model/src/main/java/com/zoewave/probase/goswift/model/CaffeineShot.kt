package com.zoewave.probase.goswift.model

import kotlinx.serialization.Serializable

@Serializable
data class CaffeineShot(
    val id: String,
    val mg: Int,
    val timestamp: Long
)
