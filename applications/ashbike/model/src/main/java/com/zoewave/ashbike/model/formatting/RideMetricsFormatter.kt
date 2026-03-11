package com.zoewave.ashbike.model.formatting

import com.zoewave.ashbike.model.R

class RideMetricsFormatter {

    fun formatSpeed(speedKmh: Float, isImperial: Boolean): DisplayMetric {
        return if (isImperial) {
            DisplayMetric(
                value = String.format("%.1f", speedKmh.toMph()),
                unitResId = R.string.applications_ashbike_model_unit_mph
            )
        } else {
            DisplayMetric(
                value = String.format("%.1f", speedKmh),
                unitResId = R.string.applications_ashbike_model_unit_kmh
            )
        }
    }

    fun formatDistance(distanceKm: Float, isImperial: Boolean): DisplayMetric {
        return if (isImperial) {
            DisplayMetric(
                value = String.format("%.2f", distanceKm.toMiles()),
                unitResId = R.string.applications_ashbike_model_unit_miles
            )
        } else {
            DisplayMetric(
                value = String.format("%.2f", distanceKm),
                unitResId = R.string.applications_ashbike_model_unit_kilometers
            )
        }
    }

    fun formatElevation(elevationMeters: Double, isImperial: Boolean): DisplayMetric {
        return if (isImperial) {
            DisplayMetric(
                value = String.format("%.0f", elevationMeters.toFeet()),
                unitResId = R.string.applications_ashbike_model_unit_feet
            )
        } else {
            DisplayMetric(
                value = String.format("%.0f", elevationMeters),
                unitResId = R.string.applications_ashbike_model_unit_meters
            )
        }
    }
}