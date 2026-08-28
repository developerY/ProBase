package com.zoewave.probase.kocolor.fashionista.color

import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

data class LabColor(val l: Double, val a: Double, val b: Double)
data class LChColor(val l: Double, val c: Double, val h: Double)

object ColorSpaceConverter {

    fun hexToLab(hex: String): LabColor {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLongOrNull(16)?.toInt() ?: 0x808080

        val r = ((colorInt shr 16) and 0xFF) / 255.0
        val g = ((colorInt shr 8) and 0xFF) / 255.0
        val b = (colorInt and 0xFF) / 255.0

        fun pivotRgb(c: Double): Double {
            return if (c > 0.04045) ((c + 0.055) / 1.055).pow(2.4) else (c / 12.92)
        }

        val x = (pivotRgb(r) * 0.4124 + pivotRgb(g) * 0.3576 + pivotRgb(b) * 0.1805) / 0.95047
        val y = (pivotRgb(r) * 0.2126 + pivotRgb(g) * 0.7152 + pivotRgb(b) * 0.0722) / 1.00000
        val z = (pivotRgb(r) * 0.0193 + pivotRgb(g) * 0.1192 + pivotRgb(b) * 0.9505) / 1.08883

        fun pivotXyz(c: Double): Double {
            return if (c > 0.008856) Math.cbrt(c) else (7.787 * c) + (16.0 / 116.0)
        }

        val fx = pivotXyz(x)
        val fy = pivotXyz(y)
        val fz = pivotXyz(z)

        val l = ((116.0 * fy) - 16.0).coerceIn(0.0, 100.0)
        val labA = 500.0 * (fx - fy)
        val labB = 200.0 * (fy - fz)

        return LabColor(l, labA, labB)
    }

    fun labToLCh(lab: LabColor): LChColor {
        val c = sqrt(lab.a * lab.a + lab.b * lab.b)
        var h = Math.toDegrees(atan2(lab.b, lab.a))
        if (h < 0) h += 360.0
        return LChColor(lab.l, c, h)
    }

    fun hexToLCh(hex: String): LChColor {
        return labToLCh(hexToLab(hex))
    }
}
