package com.zoewave.probase.kocolor.fashionista.silhouette

import android.graphics.Bitmap
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import com.zoewave.probase.kocolor.fashionista.math.Geometry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisualMassEngine @Inject constructor() {

    /**
     * Downsamples the outfit image into a 64x128 pixel buffer to compute Visual Center of Gravity (CoG).
     * Executes in < 5ms without external CV libraries.
     */
    fun processBitmap(rawBitmap: Bitmap?): FeatureValue {
        if (rawBitmap == null || rawBitmap.width == 0 || rawBitmap.height == 0) {
            return FeatureValue(value = 0.0, availability = 0.0)
        }

        val scaled = Bitmap.createScaledBitmap(rawBitmap, 64, 128, true)
        val points = mutableListOf<Geometry.Point2D>()
        val masses = DoubleArray(64 * 128)
        var idx = 0

        for (y in 0 until 128) {
            for (x in 0 until 64) {
                val pixel = scaled.getPixel(x, y)
                val alpha = (pixel shr 24) and 0xFF
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                // Chromatic mass weighting (darker/more saturated pixels have higher visual mass)
                val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                val mass = (alpha / 255.0) * (1.0 - luminance)

                points.add(Geometry.Point2D(x / 64.0, y / 128.0))
                masses[idx++] = mass
            }
        }

        val cog = Geometry.calculateCenterOfGravity(points, masses)

        // Evaluate CoG displacement from natural center (x = 0.5, y = 0.55 for human silhouette)
        val xDev = Math.abs(cog.x - 0.5)
        val yDev = Math.abs(cog.y - 0.55)

        // Asymmetry is allowed; penalize extreme ungrounded shifts
        val score = (1.0 - (xDev * 0.8 + yDev * 0.5)).coerceIn(0.0, 1.0)

        return FeatureValue(value = score, availability = 1.0)
    }
}
