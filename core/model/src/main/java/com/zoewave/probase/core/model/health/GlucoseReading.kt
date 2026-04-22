package com.zoewave.probase.core.model.health

import java.time.Instant

/**
 * Represents a single glucose reading from a CGM or BGM device.
 */
data class GlucoseReading(
    val valueMgDl: Float,      // Glucose in mg/dL
    val timestamp: Instant,
    val source: GlucoseSource,
    val type: GlucoseType = GlucoseType.CGM,
    val trendArrow: String? = null // e.g., "RISING", "STABLE"
)

/**
 * Categorizes the type of glucose monitoring system.
 */
enum class GlucoseType {
    CGM, // Continuous Glucose Monitor
    BGM  // Blood Glucose Monitor (Manual/Discrete)
}

/**
 * Identifies the source manufacturer/system for the glucose reading.
 */
enum class GlucoseSource {
    BLE_STANDARD,
    LIBRE_NFC,
    LIBRE_BLE,
    DEXCOM_SHARE,
    LIFESCAN_ONETOUCH,
    ABBOTT_LIBRE_LINK_UP,
    MEDTRONIC_CARELINK,
    ASCENSIA_CONTOUR,
    MEDTRUM,
    SIBIONICS,
    TRIVIDIA_TRUE_METRIX,
    SIMULATOR
}
