package com.zoewave.probase.kocolor.features.analyzer.calibration.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.features.analyzer.calibration.ColorSeasonClassifier
import com.zoewave.probase.kocolor.features.analyzer.calibration.LightingValidator
import com.zoewave.probase.kocolor.model.calibration.FacialContrastVector
import com.zoewave.probase.kocolor.model.calibration.ColorProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CalibrationUiState {
    data object Idle : CalibrationUiState
    data object Scanning : CalibrationUiState
    data class Success(val profile: ColorProfile) : CalibrationUiState
    data class Error(val message: String) : CalibrationUiState
}

sealed interface LightingStatus {
    data object Unknown : LightingStatus
    data object Poor : LightingStatus
    data object Optimal : LightingStatus
}

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val lightingValidator: LightingValidator,
    private val seasonClassifier: ColorSeasonClassifier,
    private val fashionRepository: FashionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalibrationUiState>(CalibrationUiState.Idle)
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()

    private val _lightingStatus = MutableStateFlow<LightingStatus>(LightingStatus.Unknown)
    val lightingStatus: StateFlow<LightingStatus> = _lightingStatus.asStateFlow()

    private val _events = MutableSharedFlow<CalibrationEvent>()
    val events: SharedFlow<CalibrationEvent> = _events.asSharedFlow()

    init {
        startLightingValidation()
    }

    private fun startLightingValidation() {
        lightingValidator.start { lux ->
            _lightingStatus.update {
                if (lightingValidator.isLightingOptimal(lux)) LightingStatus.Optimal else LightingStatus.Poor
            }
        }
    }

    fun onScanResult(vector: FacialContrastVector, undertone: Float) {
        if (_uiState.value !is CalibrationUiState.Scanning) return
        
        Log.d("CalibrationViewModel", "Scan successful: $vector, undertone: $undertone")
        
        viewModelScope.launch {
            val season = seasonClassifier.classify(vector, undertone)
            val profile = ColorProfile(
                season = season,
                undertone = undertone,
                contrastVector = vector,
                optimalPaletteHexCodes = seasonClassifier.getOptimalPalette(season)
            )
            
            Log.d("CalibrationViewModel", "Established Profile: ${profile.season}")
            
            // Save to repository
            fashionRepository.saveProfile(profile.toFashionProfile())
            
            _uiState.value = CalibrationUiState.Success(profile)
        }
    }

    fun dismissResult() {
        viewModelScope.launch {
            _events.emit(CalibrationEvent.NavigateBack)
        }
    }

    fun startScan() {
        _uiState.value = CalibrationUiState.Scanning
    }

    fun onError(message: String) {
        _uiState.value = CalibrationUiState.Error(message)
    }

    override fun onCleared() {
        super.onCleared()
        lightingValidator.stop()
    }
}

sealed interface CalibrationEvent {
    data object NavigateBack : CalibrationEvent
}
