package com.zoewave.ashbike.mobile.home.ui

sealed class HomeEvent {
    // Ride Controls
    object StartRide : HomeEvent()
    object StopRide : HomeEvent()

    object ToggleDemo : HomeEvent()

    // Bike Dashboard Interactions
    object OnBikeClick : HomeEvent()
    data class SetTotalDistance(val distanceKm: Float) : HomeEvent()
    object DismissSetDistanceDialog : HomeEvent()

    // 1. ADD THE TOGGLE EVENT
    // Glass / XR Controls
    object ToggleGlassProjection : HomeEvent()



    // Added for Option 2: Semantic Event for navigations
    // Navigation Events
    data class NavigateToSettingsRequested(val cardKey: String?) : HomeEvent()
}
