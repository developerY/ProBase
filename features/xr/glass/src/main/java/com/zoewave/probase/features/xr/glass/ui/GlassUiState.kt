package com.zoewave.probase.features.xr.glass.ui

import com.zoewave.probase.core.model.ritual.BeautyRoutine

data class GlassUiState(
    val morningRoutine: BeautyRoutine? = null,
    val isLoading: Boolean = true,
    val isAiActive: Boolean = false,
    val aiAudioLevel: Float = 0f
)
