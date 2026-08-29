package com.zoewave.probase.kocolor.fashionista.presentation

enum class AvailabilityStatus { MEASURED, PARTIAL, NOT_MEASURABLE }

data class FeatureExplanation(
    val name: String,
    val value: Int?, // Null if availability == 0.0
    val availabilityStatus: AvailabilityStatus, 
    val title: String,
    val explanation: String
)

data class FashionistaExplanation(
    val score: Int, // Rounded to nearest integer
    val coveragePercentage: Int, // e.g., 83 for 0.83 coverage
    val interpretation: String, 
    val features: List<FeatureExplanation>
)
