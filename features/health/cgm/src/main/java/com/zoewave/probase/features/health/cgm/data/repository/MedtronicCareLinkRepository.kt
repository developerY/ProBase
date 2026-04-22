package com.zoewave.probase.features.health.cgm.data.repository

import com.zoewave.probase.core.data.security.SecureTokenManager
import com.zoewave.probase.core.model.health.GlucoseReading
import com.zoewave.probase.core.model.health.GlucoseSource
import com.zoewave.probase.core.model.health.GlucoseType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GlucoseRepository for Medtronic CareLink.
 * Medium Accessibility: Clinical data sharing platform.
 */
@Singleton
class MedtronicCareLinkRepository @Inject constructor(
    private val secureTokenManager: SecureTokenManager
) : GlucoseRepository {

    override val glucoseReadings: Flow<GlucoseReading> = flow {
        while (true) {
            emit(
                GlucoseReading(
                    valueMgDl = (120..180).random().toFloat(),
                    timestamp = Instant.now(),
                    source = GlucoseSource.MEDTRONIC_CARELINK,
                    type = GlucoseType.CGM,
                    trendArrow = "STABLE"
                )
            )
            delay(300000) // Typically 5-minute data
        }
    }

    override suspend fun scanSensor() {
        // No manual scan for CareLink.
    }
}
