package com.zoewave.probase.kocolor.fashionista.math

import android.graphics.Bitmap
import com.zoewave.probase.kocolor.fashionista.color.ColorSpaceConverter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2

data class ItaResult(
    val itaScore: Double,
    val skinToneCategory: String,
    val lStar: Double,
    val bStar: Double
)

@Singleton
class ItaCalculator @Inject constructor() {

    fun calculateIta(lStar: Double, bStar: Double): ItaResult {
        val ita = Math.toDegrees(atan2(lStar - 50.0, bStar))
        val category = when {
            ita > 55.0 -> "Very Light"
            ita > 41.0 -> "Light"
            ita > 28.0 -> "Intermediate"
            ita > 10.0 -> "Tan"
            ita > -30.0 -> "Dark"
            else -> "Very Dark"
        }
        return ItaResult(
            itaScore = ita,
            skinToneCategory = category,
            lStar = lStar,
            bStar = bStar
        )
    }

    fun calculateItaFromHex(hexColor: String): ItaResult {
        val lab = ColorSpaceConverter.hexToLab(hexColor)
        return calculateIta(lab.l, lab.b)
    }

    fun calculateIta(faceBitmap: Bitmap): ItaResult {
        val centerX = faceBitmap.width / 2
        val centerY = faceBitmap.height / 2
        val pixel = faceBitmap.getPixel(centerX, centerY)
        val hex = String.format("#%06X", 0xFFFFFF and pixel)
        return calculateItaFromHex(hex)
    }
}
