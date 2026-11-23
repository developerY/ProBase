package com.zoewave.ashbike.features.ble

class WheelCrankKinematicsCalculator(
    private val wheelTimeResolutionHz: Double,
    private val crankTimeResolutionHz: Double = 1024.0,
    private val clockMs: () -> Long = { System.currentTimeMillis() },
    var wheelCircumferenceMm: Int = 2136,
    var staleTimeoutMs: Long = 3_000L
) {
    var speedKph: Double = 0.0
        private set
    var deltaDistanceMeters: Double = 0.0
        private set
    var cadenceRpm: Double = 0.0
        private set

    private var prevWheelRevs: Long? = null
    private var prevWheelTime: Int? = null
    private var prevCrankRevs: Int? = null
    private var prevCrankTime: Int? = null

    private var lastWheelUpdateMs: Long = 0L
    private var lastCrankUpdateMs: Long = 0L

    fun reset() {
        speedKph = 0.0
        deltaDistanceMeters = 0.0
        cadenceRpm = 0.0
        prevWheelRevs = null
        prevWheelTime = null
        prevCrankRevs = null
        prevCrankTime = null
        lastWheelUpdateMs = 0L
        lastCrankUpdateMs = 0L
    }

    fun updateWheel(wheelRevs: Long, lastWheelEventTimeRaw: Int) {
        val now = clockMs()
        lastWheelUpdateMs = now

        if (prevWheelRevs == null || prevWheelTime == null) {
            prevWheelRevs = wheelRevs
            prevWheelTime = lastWheelEventTimeRaw
            deltaDistanceMeters = 0.0
            speedKph = 0.0
            return
        }

        var dRevs = wheelRevs - prevWheelRevs!!
        if (dRevs < 0) { 
            // 32-bit rollover
            dRevs = (wheelRevs + 0x100000000L) - prevWheelRevs!!
        }

        val dTimeRaw = (lastWheelEventTimeRaw - prevWheelTime!!) and 0xFFFF
        val dTimeSec = dTimeRaw / wheelTimeResolutionHz

        deltaDistanceMeters = dRevs * (wheelCircumferenceMm / 1000.0)

        if (dTimeSec > 0) {
            val speedMps = deltaDistanceMeters / dTimeSec
            val calcSpeedKph = speedMps * 3.6
            if (calcSpeedKph <= 150.0) {
                speedKph = calcSpeedKph
            }
        } else if (dRevs == 0L) {
            speedKph = 0.0
        }

        prevWheelRevs = wheelRevs
        prevWheelTime = lastWheelEventTimeRaw
    }

    fun updateCrank(crankRevs: Int, lastCrankEventTimeRaw: Int) {
        val now = clockMs()
        lastCrankUpdateMs = now

        if (prevCrankRevs == null || prevCrankTime == null) {
            prevCrankRevs = crankRevs
            prevCrankTime = lastCrankEventTimeRaw
            cadenceRpm = 0.0
            return
        }

        val dRevs = (crankRevs - prevCrankRevs!!) and 0xFFFF
        val dTimeRaw = (lastCrankEventTimeRaw - prevCrankTime!!) and 0xFFFF
        val dTimeSec = dTimeRaw / crankTimeResolutionHz

        if (dTimeSec > 0) {
            val calcRpm = (dRevs / dTimeSec) * 60.0
            if (calcRpm <= 250.0) {
                cadenceRpm = calcRpm
            }
        } else if (dRevs == 0) {
            cadenceRpm = 0.0
        }

        prevCrankRevs = crankRevs
        prevCrankTime = lastCrankEventTimeRaw
    }

    fun tick() {
        val now = clockMs()
        if (now - lastWheelUpdateMs > staleTimeoutMs) {
            speedKph = 0.0
        }
        if (now - lastCrankUpdateMs > staleTimeoutMs) {
            cadenceRpm = 0.0
        }
    }
}
