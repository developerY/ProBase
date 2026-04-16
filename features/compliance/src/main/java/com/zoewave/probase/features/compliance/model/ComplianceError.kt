package com.zoewave.probase.features.compliance.model

/**
 * Custom error types for Compliance features.
 */
sealed class ComplianceError : Exception() {
    object SdkVersionOutdated : ComplianceError() {
        override val message: String = "Play Age Signals SDK is outdated and requires an update."
    }
    object NetworkError : ComplianceError() {
        override val message: String = "A network error occurred while retrieving age signals."
    }
    data class GenericError(val originalMessage: String?) : ComplianceError() {
        override val message: String = originalMessage ?: "An unknown compliance error occurred."
    }
}
