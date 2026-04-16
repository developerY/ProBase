package com.zoewave.probase.features.compliance.model

import java.util.Date

/**
 * Decoupled model for Age Signals.
 */
data class AgeSignal(
    val ageRange: AgeRange?,
    val verificationStatus: AgeVerificationStatus,
    val mostRecentApprovalDate: Date?
)

enum class AgeRange(val description: String) {
    AGE_0_12("0-12"),
    AGE_13_15("13-15"),
    AGE_16_17("16-17"),
    AGE_18_PLUS("18+"),
    UNKNOWN("Unknown")
}

enum class AgeVerificationStatus {
    VERIFIED,
    DECLARED,
    SUPERVISED,
    SUPERVISED_APPROVAL_PENDING,
    SUPERVISED_APPROVAL_DENIED,
    NOT_APPLICABLE,
    UNKNOWN
}
