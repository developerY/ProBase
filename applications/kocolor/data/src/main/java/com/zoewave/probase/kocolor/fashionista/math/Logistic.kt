package com.zoewave.probase.kocolor.fashionista.math

import kotlin.math.exp

object Logistic {

    /**
     * Standard logistic sigmoid scaled to [0.0, 100.0].
     * F = 100 * (1 / (1 + exp(-(q - mu) / tau)))
     */
    fun logisticScaling(q: Double, mu: Double, tau: Double): Double {
        if (tau == 0.0) return 50.0
        val z = (q - mu) / tau
        val sigmoid = 1.0 / (1.0 + exp(-z))
        return (100.0 * sigmoid).coerceIn(0.0, 100.0)
    }
}
