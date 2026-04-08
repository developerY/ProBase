package com.zoewave.probase.goswift.wear.input.ui

sealed interface AddShotUiState {
    object Idle : AddShotUiState
    object Loading : AddShotUiState
    object Success : AddShotUiState
}
