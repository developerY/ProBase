package com.zoewave.probase.ashbike.wear.features.rides

// --- UI EVENTS ---
// Represents every action the user can take on the Wear screen
sealed interface BikeUiEvent {
    data object StartRide : BikeUiEvent
    data object PauseRide : BikeUiEvent
    data object ResumeRide : BikeUiEvent
    data object StopRide : BikeUiEvent
    data class AcknowledgeError(val errorId: String) : BikeUiEvent
}