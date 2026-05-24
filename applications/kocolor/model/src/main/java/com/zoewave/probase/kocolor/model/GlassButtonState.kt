package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

@Serializable
enum class GlassButtonState {
    NO_GLASSES,
    READY_TO_START,
    PROJECTING
}
