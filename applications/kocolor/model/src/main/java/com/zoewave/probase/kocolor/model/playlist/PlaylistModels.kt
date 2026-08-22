package com.zoewave.probase.kocolor.model.playlist

import kotlinx.serialization.Serializable

@Serializable
enum class PlaylistStatus {
    GENERATED, PREVIEWED, ACCEPTED, LOCKED, COMPLETED, DISCARDED
}

@Serializable
enum class DailyPlanStatus {
    PLANNED, ROUTED, WORN, COMMITTED, SKIPPED
}
