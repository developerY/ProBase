package com.zoewave.probase.features.compliance

import com.zoewave.probase.features.compliance.model.AgeSignal

/**
 * Interface for retrieving Play Age Signals.
 */
interface AgeSignalsManager {
    /**
     * Retrieves the current user's age signal from the Play Store.
     * This should be called at runtime and not cached long-term.
     *
     * @return Result containing [AgeSignal] or a [ComplianceError].
     */
    suspend fun getAgeSignal(): Result<AgeSignal>
}
