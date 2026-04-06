package com.zoewave.probase.goswift.features.main.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class GoSwiftDestination {
    @Serializable
    data object Home : GoSwiftDestination()
    @Serializable
    data object Shots : GoSwiftDestination()
    @Serializable
    data object AddShot : GoSwiftDestination()
    @Serializable
    data object Settings : GoSwiftDestination()
    @Serializable
    data object Hydration : GoSwiftDestination()
}
