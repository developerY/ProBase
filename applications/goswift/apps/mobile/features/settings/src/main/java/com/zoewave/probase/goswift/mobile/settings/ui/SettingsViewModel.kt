package com.zoewave.probase.goswift.mobile.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.goswift.data.ShotRepository
import com.zoewave.probase.goswift.data.ThemeMode
import com.zoewave.probase.goswift.data.ThemeRepository
import com.zoewave.probase.goswift.model.CaffeineShot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Random
import java.util.UUID
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
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
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
    val isHealthConnectConnected: Boolean = false,
    val cacheSizeMb: Double = 52.8,
    val toastMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val shotRepository: ShotRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepository.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        themeRepository.setThemeMode(themeMode)
    }

    fun updateHealthConnectStatus(connected: Boolean) {
        _uiState.update { it.copy(isHealthConnectConnected = connected) }
    }

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

    fun generateDebugData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val random = Random()
            val mgOptions = intArrayOf(15, 20, 25, 35, 50, 65)

            // Generate ~2 to 4 shots per day for the last 21 days (3 weeks)
            for (day in 0 until 21) {
                val dayStart = now - (day * 24 * 60 * 60 * 1000L)
                val shotsCount = 2 + random.nextInt(3)
                repeat(shotsCount) {
                    val randomOffset = kotlin.math.abs(random.nextLong()) % (14 * 60 * 60 * 1000L)
                    val shotTimestamp = dayStart - randomOffset
                    val mg = mgOptions[random.nextInt(mgOptions.size)]
                    shotRepository.addShot(
                        CaffeineShot(
                            id = UUID.randomUUID().toString(),
                            mg = mg,
                            timestamp = shotTimestamp
                        )
                    )
                }
            }
            _uiState.update { it.copy(toastMessage = "Seeded 3 weeks of random telemetry & shots!") }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
