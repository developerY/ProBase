package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
enum class SpendingImportance {
    REQUIRED, // Needed / Essential
    OPTIONAL  // Wanted / Non-essential
}
