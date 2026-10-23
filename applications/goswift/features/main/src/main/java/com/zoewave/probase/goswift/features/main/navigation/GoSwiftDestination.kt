package com.zoewave.probase.goswift.features.main.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class GoSwiftDestination {
    @Serializable
    data object Home : GoSwiftDestination()

    @Serializable
    data object Log : GoSwiftDestination()

    @Serializable
    data object Settings : GoSwiftDestination()

    @Serializable
    data object Schedule : GoSwiftDestination()

    @Serializable
    data object Insights : GoSwiftDestination()

    @Serializable
    data object HealthSync : GoSwiftDestination()
}
