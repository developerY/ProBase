package com.zoewave.probase.features.health.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class RxCuiResponse(
    val idGroup: IdGroup? = null
)

@Serializable
data class IdGroup(
    val rxnormId: List<String>? = null
)

@Serializable
data class InteractionResponse(
    val interactionTypeGroup: List<InteractionTypeGroup>? = null
)

@Serializable
data class InteractionTypeGroup(
    val interactionType: List<InteractionType>? = null
)

@Serializable
data class InteractionType(
    val interactionPair: List<InteractionPair>? = null
)

@Serializable
data class InteractionPair(
    val description: String? = null
)
