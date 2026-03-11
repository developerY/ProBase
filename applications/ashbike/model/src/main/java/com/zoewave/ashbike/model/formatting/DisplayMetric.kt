package com.zoewave.ashbike.model.formatting

import androidx.annotation.StringRes

/**
 * Holds a pre-formatted number and the safely localized resource ID for its unit.
 */
data class DisplayMetric(
    val value: String,
    @StringRes val unitResId: Int
)