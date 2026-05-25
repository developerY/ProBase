package com.zoewave.probase.features.xr.glass.ui

sealed class GlassUiEvent {
    data class ToggleStep(val stepId: String) : GlassUiEvent()
    data object ToggleAi : GlassUiEvent()
    data object CloseApp : GlassUiEvent()
}
