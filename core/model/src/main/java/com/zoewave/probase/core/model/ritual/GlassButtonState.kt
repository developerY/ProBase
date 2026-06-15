package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

@Serializable
enum class GlassButtonState {
    NO_GLASSES,
    READY_TO_START,
    PROJECTING
}
