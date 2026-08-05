package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SignedPayloadEnvelope<T>(
    val data: T,
    val signature: String,
    val version: String
)
