package com.zoewave.ashbike.data.services


import com.zoewave.ashbike.model.bike.LocationPoint
import kotlinx.coroutines.flow.StateFlow

/**
 * The core hardware-agnostic contract for tracking a bike ride.
 * * By abstracting the tracking engine, the shared BikeForegroundService
 * can orchestrate a ride without knowing if it is running on a phone
 * (using standard LocationManager & BLE) or a smartwatch (using Health Services).
 */
interface RideTrackingEngine {

    /** * Emits the current heart rate in BPM.
     * Emits 0 if no sensor is available or the watch is off the wrist.
     */
    val currentHeartRate: StateFlow<Int>

    /** * Emits the most recent GPS location point.
     * Emits null while waiting for the initial satellite lock.
     */
    val currentLocation: StateFlow<LocationPoint?>

    /** * Commands the underlying hardware (ExerciseClient or LocationManager)
     * to power up the GNSS chip and heart rate sensors and begin streaming data.
     */
    fun startRide()

    /** * Commands the underlying hardware to shut down the sensors, unregister
     * callbacks, and conserve battery.
     */
    fun stopRide()
}