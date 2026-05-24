package com.zoewave.kocolor.mobile.glass.ui

sealed class GlassUiEvent {
    data class ToggleStep(val stepId: String) : GlassUiEvent()
    data object CloseApp : GlassUiEvent()
}
