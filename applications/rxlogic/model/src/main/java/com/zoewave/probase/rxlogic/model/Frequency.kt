package com.zoewave.probase.rxlogic.model

import kotlinx.serialization.Serializable

@Serializable
enum class Frequency {
    DAILY,
    WEEKLY,
    AS_NEEDED
}
