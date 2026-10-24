package com.zoewave.probase.goswift.mobile.home.ui

data class HomeUiState(
    val remainingMg: Double = 0.0,
    val limitMg: Double = 400.0,
    val heartRateBpm: Long? = null,
    val hrvMs: Double? = null,
    val glucoseMgDl: Double? = null,
    val hydrationMl: Double = 1850.0,
    val circadianIndex: Int = 88,
    val isLoading: Boolean = false
)
