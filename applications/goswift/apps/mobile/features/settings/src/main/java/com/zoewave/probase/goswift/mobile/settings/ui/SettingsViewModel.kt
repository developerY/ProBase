package com.zoewave.probase.goswift.mobile.settings.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class MicrodoseQuantum(val label: String, val mgText: String) {
    GENTLE_10MG("10mg\nGentle", "10mg"),
    STANDARD_15MG("15mg\nStandard", "15mg"),
    FOCUS_20MG("20mg\nFocus", "20mg"),
    SPRINT_25MG("25mg\nSprint", "25mg");

    companion object {
        fun fromMgText(text: String): MicrodoseQuantum {
            return entries.find { it.mgText == text } ?: STANDARD_15MG
        }
    }
}

enum class HrvSensitivity(val label: String) {
    LOW("Low"),
    BALANCED("Balanced"),
    AGGRESSIVE("Aggressive");

    companion object {
        fun fromLabel(text: String): HrvSensitivity {
            return entries.find { it.label == text } ?: AGGRESSIVE
        }
    }
}

enum class HapticPattern(val label: String) {
    SUBTLE("Subtle"),
    CRISP("Crisp"),
    TRIPLE_PULSE("Triple-Pulse");

    companion object {
        fun fromLabel(text: String): HapticPattern {
            return entries.find { it.label == text } ?: CRISP
        }
    }
}

data class SettingsUiState(
    val microdoseQuantum: MicrodoseQuantum = MicrodoseQuantum.STANDARD_15MG,
    val caffeineCeilingMg: Float = 120f,
    val curfewEnabled: Boolean = true,
    val onDeviceInferenceEnabled: Boolean = true,
    val hrvSensitivity: HrvSensitivity = HrvSensitivity.AGGRESSIVE,
    val postPrandialResponseEnabled: Boolean = true,
    val calendarAnticipationEnabled: Boolean = true,
    val hapticPattern: HapticPattern = HapticPattern.CRISP,
    val bioPromptsEnabled: Boolean = true,
    val slumpInterventionsEnabled: Boolean = true,
    val isHealthConnectConnected: Boolean = true,
    val cacheSizeMb: Double = 52.8,
    val toastMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateMicrodoseQuantum(quantum: MicrodoseQuantum) {
        _uiState.update { it.copy(microdoseQuantum = quantum) }
    }

    fun updateCaffeineCeiling(ceilingMg: Float) {
        _uiState.update { it.copy(caffeineCeilingMg = ceilingMg) }
    }

    fun toggleCurfew(enabled: Boolean) {
        _uiState.update { it.copy(curfewEnabled = enabled) }
    }

    fun toggleOnDeviceInference(enabled: Boolean) {
        _uiState.update { it.copy(onDeviceInferenceEnabled = enabled) }
    }

    fun updateHrvSensitivity(sensitivity: HrvSensitivity) {
        _uiState.update { it.copy(hrvSensitivity = sensitivity) }
    }

    fun togglePostPrandialResponse(enabled: Boolean) {
        _uiState.update { it.copy(postPrandialResponseEnabled = enabled) }
    }

    fun toggleCalendarAnticipation(enabled: Boolean) {
        _uiState.update { it.copy(calendarAnticipationEnabled = enabled) }
    }

    fun updateHapticPattern(pattern: HapticPattern) {
        _uiState.update { it.copy(hapticPattern = pattern) }
    }

    fun toggleBioPrompts(enabled: Boolean) {
        _uiState.update { it.copy(bioPromptsEnabled = enabled) }
    }

    fun toggleSlumpInterventions(enabled: Boolean) {
        _uiState.update { it.copy(slumpInterventionsEnabled = enabled) }
    }

    fun purgeCache() {
        _uiState.update { it.copy(cacheSizeMb = 0.0, toastMessage = "Local inference cache purged") }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
