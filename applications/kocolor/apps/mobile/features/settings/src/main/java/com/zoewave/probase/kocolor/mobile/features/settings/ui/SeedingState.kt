package com.zoewave.probase.kocolor.mobile.features.settings.ui

sealed class SeedingState {
    data object Idle : SeedingState()
    data object Loading : SeedingState()
    data object Success : SeedingState()
    data class Error(val message: String) : SeedingState()
}
