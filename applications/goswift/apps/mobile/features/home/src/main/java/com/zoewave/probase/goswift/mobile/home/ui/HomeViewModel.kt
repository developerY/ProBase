package com.zoewave.probase.goswift.mobile.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.goswift.data.ShotRepository
import com.zoewave.probase.goswift.model.CaffeineShot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ShotRepository
) : ViewModel() {

    private val halfLifeHours = 5.0
    private val decayConstant = ln(2.0) / halfLifeHours

    val uiState: StateFlow<HomeUiState> = repository.getAllShots()
        .map { shots ->
            val currentTime = System.currentTimeMillis()
            val currentLevel = calculateCurrentCaffeine(shots, currentTime)
            
            HomeUiState.Success(
                currentCaffeineMg = currentLevel.toInt(),
                nextDoseRecommendation = getRecommendation(currentLevel),
                sleepQualityImpact = getSleepImpact(currentLevel)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    private fun calculateCurrentCaffeine(shots: List<CaffeineShot>, currentTime: Long): Double {
        return shots.sumOf { shot ->
            val hoursPassed = (currentTime - shot.timestamp).toDouble() / TimeUnit.HOURS.toMillis(1)
            if (hoursPassed < 0) 0.0 else shot.mg * (0.5).pow(hoursPassed / halfLifeHours)
        }
    }

    private fun getRecommendation(currentLevel: Double): String {
        return if (currentLevel < 50) "Time for a 20mg micro-dose!" else "Energy level optimal. Wait 2 hours."
    }

    private fun getSleepImpact(currentLevel: Double): String {
        return if (currentLevel > 100) "High levels might disrupt sleep." else "Safe for a good night's rest."
    }

    fun onEvent(event: HomeUiEvent) {
        // Refresh handled by repository flow
    }
}
