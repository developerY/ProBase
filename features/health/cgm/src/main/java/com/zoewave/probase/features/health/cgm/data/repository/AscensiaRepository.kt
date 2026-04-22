package com.zoewave.probase.features.health.cgm.data.repository

import com.zoewave.probase.core.data.security.SecureTokenManager
import com.zoewave.probase.core.model.health.GlucoseReading
import com.zoewave.probase.core.model.health.GlucoseSource
import com.zoewave.probase.core.model.health.GlucoseType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GlucoseRepository for Ascensia Contour.
 * Medium Accessibility: BGM/CGM via partner platforms.
 */
@Singleton
class AscensiaRepository @Inject constructor(
    private val secureTokenManager: SecureTokenManager
) : GlucoseRepository {

    private val _glucoseReadings = MutableSharedFlow<GlucoseReading>(replay = 1)
    override val glucoseReadings: Flow<GlucoseReading> = _glucoseReadings.asSharedFlow()

    override suspend fun scanSensor() {
        _glucoseReadings.emit(
            GlucoseReading(
                valueMgDl = (95..125).random().toFloat(),
                timestamp = Instant.now(),
                source = GlucoseSource.ASCENSIA_CONTOUR,
                type = GlucoseType.BGM
            )
        )
    }
}
