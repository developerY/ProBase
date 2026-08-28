package com.zoewave.probase.kocolor.fashionista.math

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object Distance {

    fun euclidean(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Exact CIEDE2000 (Delta E 00) perceptual color distance between two CIELAB colors (L1, a1, b1) and (L2, a2, b2).
     */
    fun ciede2000(
        l1: Double, a1: Double, b1: Double,
        l2: Double, a2: Double, b2: Double
    ): Double {
        val kL = 1.0
        val kC = 1.0
        val kH = 1.0

        val c1 = sqrt(a1 * a1 + b1 * b1)
        val c2 = sqrt(a2 * a2 + b2 * b2)
        val cBar = (c1 + c2) / 2.0

        val cBar7 = cBar.pow(7.0)
        val g = 0.5 * (1.0 - sqrt(cBar7 / (cBar7 + 6103515625.0)))

        val a1Prime = (1.0 + g) * a1
        val a2Prime = (1.0 + g) * a2

        val c1Prime = sqrt(a1Prime * a1Prime + b1 * b1)
        val c2Prime = sqrt(a2Prime * a2Prime + b2 * b2)

        val h1Prime = if (b1 == 0.0 && a1Prime == 0.0) 0.0 else Math.toDegrees(atan2(b1, a1Prime)).let { if (it < 0) it + 360.0 else it }
        val h2Prime = if (b2 == 0.0 && a2Prime == 0.0) 0.0 else Math.toDegrees(atan2(b2, a2Prime)).let { if (it < 0) it + 360.0 else it }

        val deltaLPrime = l2 - l1
        val deltaCPrime = c2Prime - c1Prime

        var deltahPrime = if (c1Prime * c2Prime == 0.0) 0.0 else {
            val diff = h2Prime - h1Prime
            when {
                abs(diff) <= 180.0 -> diff
                diff > 180.0 -> diff - 360.0
                else -> diff + 360.0
            }
        }

        val deltaHPrime = 2.0 * sqrt(c1Prime * c2Prime) * sin(Math.toRadians(deltahPrime / 2.0))

        val lBarPrime = (l1 + l2) / 2.0
        val cBarPrime = (c1Prime + c2Prime) / 2.0

        val hBarPrime = when {
            c1Prime * c2Prime == 0.0 -> h1Prime + h2Prime
            abs(h1Prime - h2Prime) <= 180.0 -> (h1Prime + h2Prime) / 2.0
            h1Prime + h2Prime < 360.0 -> (h1Prime + h2Prime + 360.0) / 2.0
            else -> (h1Prime + h2Prime - 360.0) / 2.0
        }

        val t = 1.0 - 0.17 * cos(Math.toRadians(hBarPrime - 30.0)) +
                0.24 * cos(Math.toRadians(2.0 * hBarPrime)) +
                0.32 * cos(Math.toRadians(3.0 * hBarPrime + 6.0)) -
                0.20 * cos(Math.toRadians(4.0 * hBarPrime - 63.0))

        val deltaTheta = 30.0 * exp(-((hBarPrime - 275.0) / 25.0).pow(2.0))
        val cBarPrime7 = cBarPrime.pow(7.0)
        val rT = -2.0 * sqrt(cBarPrime7 / (cBarPrime7 + 6103515625.0)) * sin(Math.toRadians(2.0 * deltaTheta))

        val sL = 1.0 + (0.015 * (lBarPrime - 50.0).pow(2.0)) / sqrt(20.0 + (lBarPrime - 50.0).pow(2.0))
        val sC = 1.0 + 0.045 * cBarPrime
        val sH = 1.0 + 0.015 * cBarPrime * t

        val dL = deltaLPrime / (kL * sL)
        val dC = deltaCPrime / (kC * sC)
        val dH = deltaHPrime / (kH * sH)

        return sqrt(dL * dL + dC * dC + dH * dH + rT * dC * dH)
    }
}
