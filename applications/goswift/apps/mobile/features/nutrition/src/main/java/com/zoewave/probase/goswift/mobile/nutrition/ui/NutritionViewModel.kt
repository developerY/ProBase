package com.zoewave.probase.goswift.mobile.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.goswift.data.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NutritionUiState> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            getDailyNutritionFlow()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NutritionUiState.Loading
        )

    private fun getDailyNutritionFlow() = flow {
        val now = Instant.now()
        val startOfDay = now.truncatedTo(ChronoUnit.DAYS)
        val records = nutritionRepository.getNutritionRecords(startOfDay, now)
        
        val logs = records.map { 
            MealLog(
                id = it.metadata.id,
                name = it.name ?: "Unknown Food",
                calories = it.energy?.inKilocalories ?: 0.0,
                timestamp = it.startTime.toEpochMilli()
            )
        }.sortedByDescending { it.timestamp }
        
        emit(NutritionUiState.Success(logs.sumOf { it.calories }, logs))
    }

    fun onEvent(event: NutritionUiEvent) {
        when (event) {
            is NutritionUiEvent.AddMeal -> {
                viewModelScope.launch {
                    nutritionRepository.addNutritionRecord(event.name, event.calories, Instant.now())
                    refreshTrigger.emit(Unit)
                }
            }
            NutritionUiEvent.Refresh -> {
                viewModelScope.launch {
                    refreshTrigger.emit(Unit)
                }
            }
        }
    }
}
