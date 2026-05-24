package com.zoewave.kocolor.mobile.glass.ui

import com.zoewave.probase.kocolor.model.BeautyRoutine

data class GlassUiState(
    val morningRoutine: BeautyRoutine? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
