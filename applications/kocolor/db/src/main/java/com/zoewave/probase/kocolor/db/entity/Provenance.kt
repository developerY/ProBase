package com.zoewave.probase.kocolor.db.entity

import com.zoewave.probase.core.model.ritual.VerificationState

data class Provenance(
    val packId: String,
    val packageVersion: String,
    val schemaVersion: Int,
    val publisher: String,
    val installedAtTimestamp: Long,
    val verificationState: VerificationState
)
