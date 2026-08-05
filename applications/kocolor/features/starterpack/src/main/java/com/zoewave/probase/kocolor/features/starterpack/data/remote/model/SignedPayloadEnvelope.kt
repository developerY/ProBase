package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SignedPayloadEnvelope(
    val signature: String, // Base64 encoded ECDSA signature
    val payload: JsonElement // The actual JSON data (Manifest or StarterPackResponse)
)
