package com.zoewave.probase.ashbike.wear.features.home

import androidx.annotation.StringRes
import com.zoewave.ashbike.model.R
import com.zoewave.ashbike.model.formatting.DisplayMetric

data class WearBikeUiState(
    // Speedometer Canvas properties (Needs Floats for the math)
    val currentSpeed: Float = 0f,
    val maxSpeed: Float = 60f,
    @StringRes val speedUnitResId: Int = R.string.applications_ashbike_model_unit_kmh,

    // Text Display properties (Needs pre-formatted strings)
    val distance: DisplayMetric = DisplayMetric("0.00", R.string.applications_ashbike_model_unit_kilometers),
    val elevation: DisplayMetric = DisplayMetric("0", R.string.applications_ashbike_model_unit_meters),
    val elevationGain: DisplayMetric = DisplayMetric("0", R.string.applications_ashbike_model_unit_meters),

    // Unchanged properties
    val heartRate: Int = 0,
    val calories: Int = 0,
    val isTracking: Boolean = false
)