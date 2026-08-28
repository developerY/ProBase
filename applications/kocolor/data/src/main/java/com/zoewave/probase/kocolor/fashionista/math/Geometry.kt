package com.zoewave.probase.kocolor.fashionista.math

object Geometry {

    data class Point2D(val x: Double, val y: Double)

    /**
     * Calculates the Visual Center of Gravity (CoG):
     * xBar = sum(m_i * x_i) / sum(m_i)
     * yBar = sum(m_i * y_i) / sum(m_i)
     */
    fun calculateCenterOfGravity(points: List<Point2D>, masses: DoubleArray): Point2D {
        if (points.isEmpty() || points.size != masses.size) return Point2D(0.5, 0.5)

        var totalMass = 0.0
        var sumMX = 0.0
        var sumMY = 0.0

        for (i in points.indices) {
            val m = masses[i]
            totalMass += m
            sumMX += m * points[i].x
            sumMY += m * points[i].y
        }

        return if (totalMass > 0.0) {
            Point2D(sumMX / totalMass, sumMY / totalMass)
        } else {
            Point2D(0.5, 0.5)
        }
    }
}
