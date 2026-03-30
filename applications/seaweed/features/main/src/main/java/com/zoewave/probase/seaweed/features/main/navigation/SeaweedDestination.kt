package com.zoewave.probase.seaweed.features.main.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class SeaweedDestination {
    @Serializable
    data object Home : SeaweedDestination()
    @Serializable
    data object CategoryGrid : SeaweedDestination()
    @Serializable
    data class Transactions(
        val category: String? = null,
        val transactionId: String? = null
    ) : SeaweedDestination()
    @Serializable
    data object AddTransaction : SeaweedDestination()
    @Serializable
    data object Settings : SeaweedDestination()
}
