package com.zoewave.probase.kocolor.features.analyzer.calibration

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import javax.inject.Inject

class LightingValidator @Inject constructor(
    private val sensorManager: SensorManager
) {
    private var currentLux: Float = 0f
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private var listener: SensorEventListener? = null

    fun start(onLuxChanged: (Float) -> Unit) {
        if (lightSensor == null) return
        
        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.get(0)?.let { lux ->
                    currentLux = lux
                    onLuxChanged(lux)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        listener?.let {
            sensorManager.unregisterListener(it)
        }
        listener = null
    }

    fun isLightingOptimal(lux: Float = currentLux): Boolean {
        return lux in 300f..10000f
    }
}
