package com.zoewave.probase.kocolor.fashionista.math

object Normalization {

    fun clamp(value: Double, min: Double = 0.0, max: Double = 1.0): Double {
        return value.coerceIn(min, max)
    }

    fun minMaxNormalize(value: Double, min: Double, max: Double): Double {
        if (max <= min) return 0.0
        return ((value - min) / (max - min)).coerceIn(0.0, 1.0)
    }

    fun safeDivide(numerator: Double, denominator: Double, fallback: Double = 0.0): Double {
        return if (denominator != 0.0 && !denominator.isNaN()) {
            numerator / denominator
        } else {
            fallback
        }
    }
}
