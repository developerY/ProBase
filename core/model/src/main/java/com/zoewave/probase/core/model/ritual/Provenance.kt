package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

@Serializable
enum class VerificationState {
    VERIFIED,
    FAILED,
    UNKNOWN,
    LEGACY
}

@Serializable
data class Provenance(
    val packId: String,
    val packageVersion: String,
    val schemaVersion: Int,
    val publisher: String,
    val installedAtTimestamp: Long,
    val verificationState: VerificationState
)
