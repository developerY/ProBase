package com.zoewave.probase.kocolor.features.colors.domain.model

data class LabValue(val l: Double, val a: Double, val b: Double)
data class HsvValue(val h: Float, val s: Float, val v: Float)

data class PantoneMatch(
    val code: String,
    val name: String,
    val distance: Double
)

data class ColorInfo(
    val hex: String,
    val name: String,
    val pantoneMatch: PantoneMatch?,
    val complementaryPalette: List<String>,
    val cielab: LabValue? = null,
    val hsv: HsvValue? = null,
    val isProfessionalGrade: Boolean = false
)
