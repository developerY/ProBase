package com.zoewave.probase.seaweed.features.main.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class SeaweedDestination {
    @Serializable
    data object Home : SeaweedDestination()
    @Serializable
    data object AddTransaction : SeaweedDestination()
}
