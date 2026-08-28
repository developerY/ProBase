package com.zoewave.probase.kocolor.fashionista.math

import kotlin.math.sqrt

object Statistics {

    fun mean(values: DoubleArray): Double {
        if (values.isEmpty()) return 0.0
        return values.average()
    }

    fun weightedMean(values: DoubleArray, weights: DoubleArray): Double {
        if (values.isEmpty() || values.size != weights.size) return 0.0
        var sumWV = 0.0
        var sumW = 0.0
        for (i in values.indices) {
            sumWV += values[i] * weights[i]
            sumW += weights[i]
        }
        return if (sumW > 0.0) sumWV / sumW else 0.0
    }

    fun variance(values: DoubleArray): Double {
        if (values.size <= 1) return 0.0
        val avg = mean(values)
        var sumSquareDiff = 0.0
        for (v in values) {
            val diff = v - avg
            sumSquareDiff += diff * diff
        }
        return sumSquareDiff / values.size
    }

    fun standardDeviation(values: DoubleArray): Double {
        return sqrt(variance(values))
    }
}
