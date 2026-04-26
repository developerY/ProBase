package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
enum class SpendingType {
    NEED, // Required / Essential
    WANT   // Optional / Non-essential
}
