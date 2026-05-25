package com.zoewave.probase.features.xr.glass.ui

import com.zoewave.probase.kocolor.model.BeautyRoutine

data class GlassUiState(
    val morningRoutine: BeautyRoutine? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isAiActive: Boolean = false,
    val aiAudioLevel: Float = 0f
)
