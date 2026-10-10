package com.zoewave.probase.kocolor.features.travel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.travel.LocationRepository
import com.zoewave.probase.core.data.repository.weather.AtmosphericRepository
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.usecase.GenerateStyleResultUseCase
import com.zoewave.probase.kocolor.data.usecase.IntentAnalyzer
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TravelCapsuleUiState(
    val destination: String = "",
    val occasion: String = "Vacation",
    val isInternational: Boolean = false,
    val isLoading: Boolean = false,
    val isGenerated: Boolean = false,
    val passportReminder: Boolean = false,
    val weatherForecast: String = "",
    val rationale: String = "",
    val recommendedClothing: List<ClothingItem> = emptyList(),
    val recommendedCosmetics: List<CosmeticItem> = emptyList()
)

sealed class TravelEvent {
    data class UpdateDestination(val dest: String) : TravelEvent()
    data class UpdateOccasion(val occasion: String) : TravelEvent()
    data object GenerateCapsule : TravelEvent()
    data object Reset : TravelEvent()
}

@HiltViewModel
class TravelCapsuleViewModel @Inject constructor(
    private val atmosphericRepository: AtmosphericRepository,
    private val generateStyleResultUseCase: GenerateStyleResultUseCase,
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val intentAnalyzer: IntentAnalyzer
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelCapsuleUiState())
    val uiState: StateFlow<TravelCapsuleUiState> = _uiState

    fun onEvent(event: TravelEvent) {
        when (event) {
            is TravelEvent.UpdateDestination -> {
                _uiState.value = _uiState.value.copy(destination = event.dest)
            }
            is TravelEvent.UpdateOccasion -> {
                _uiState.value = _uiState.value.copy(occasion = event.occasion)
            }
            TravelEvent.GenerateCapsule -> {
                generateCapsule()
            }
            TravelEvent.Reset -> {
                _uiState.value = TravelCapsuleUiState()
            }
        }
    }

    private fun generateCapsule() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val destination = _uiState.value.destination
            val occasion = _uiState.value.occasion

            // Check if international (naive heuristic for demo: contains specific keywords or simple logic)
            // Ideally, compare `currentLocation` country code with `destination` country code.
            val isInternational = destination.contains(Regex("london|paris|tokyo|mexico|spain|italy", RegexOption.IGNORE_CASE))

            // Fetch forecast
            val forecast = atmosphericRepository.fetchTravelForecast(destination, 0L, 0L)
            val temp = forecast?.weather?.main?.temp?.toFloat() ?: 22f
            val weatherDesc = forecast?.weather?.weather?.firstOrNull()?.main ?: "Clear"

            val intentProfile = intentAnalyzer.analyze("$occasion in $destination. Weather: $weatherDesc")

            val context = StyleRequestContext(
                intent = "$occasion in $destination",
                occasion = occasion,
                intentProfile = intentProfile,
                weather = weatherDesc,
                weatherTempC = temp
            )

            val wardrobe = wardrobeRepository.getAllClothing().first()
            val cosmetics = cosmeticRepository.getAllCosmetics().first()

            try {
                val result = generateStyleResultUseCase.execute(wardrobe, cosmetics, context)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isGenerated = true,
                    isInternational = isInternational,
                    passportReminder = isInternational,
                    weatherForecast = "${temp.toInt()}°C, $weatherDesc",
                    rationale = result.blueprint.rationale,
                    recommendedClothing = result.selectedClothing,
                    recommendedCosmetics = result.selectedCosmetics
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
