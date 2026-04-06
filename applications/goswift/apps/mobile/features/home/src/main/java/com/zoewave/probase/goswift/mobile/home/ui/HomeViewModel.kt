package com.zoewave.probase.goswift.mobile.home.ui

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.goswift.data.HealthRepository
import com.zoewave.probase.goswift.data.HydrationRepository
import com.zoewave.probase.goswift.data.ShotRepository
import com.zoewave.probase.goswift.model.CaffeineShot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ShotRepository,
    private val healthRepository: HealthRepository,
    private val hydrationRepository: HydrationRepository
) : ViewModel() {

    private val halfLifeHours = 5.0
    private val decayConstant = ln(2.0) / halfLifeHours

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val healthDataFlow = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                val now = Instant.now()
                val yesterday = now.minus(24, ChronoUnit.HOURS)
                val sleepSessions = healthRepository.getSleepSessions(yesterday, now)
                val exerciseSessions = healthRepository.getExerciseSessions(yesterday, now)
                
                val totalSleepMillis = sleepSessions.sumOf { session ->
                    Duration.between(session.startTime, session.endTime).toMillis()
                }
                val totalExerciseMinutes = exerciseSessions.sumOf { session ->
                    Duration.between(session.startTime, session.endTime).toMinutes()
                }.toInt()
                
                val hydrationRecords = hydrationRepository.getHydrationRecords(yesterday, now)
                val totalHydrationLiters = hydrationRecords.sumOf { it.volume.inLiters }
                
                emit(HealthData(totalSleepMillis, totalExerciseMinutes, totalHydrationLiters))
            }
        }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllShots(),
        healthDataFlow
    ) { shots, healthData ->
        val currentTime = System.currentTimeMillis()
        val currentLevel = calculateCurrentCaffeine(shots, currentTime)
        
        HomeUiState.Success(
            currentCaffeineMg = currentLevel.toInt(),
            nextDoseRecommendation = getRecommendation(currentLevel, healthData.exerciseMinutes, healthData.hydrationLiters),
            sleepQualityImpact = getSleepImpact(currentLevel, healthData.sleepMillis),
            sleepDuration = formatDuration(healthData.sleepMillis),
            exerciseMinutes = healthData.exerciseMinutes,
            hydrationProgress = healthData.hydrationLiters
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    data class HealthData(val sleepMillis: Long, val exerciseMinutes: Int, val hydrationLiters: Double)

    private fun formatDuration(millis: Long): String {
        val hours = millis / 3_600_000
        val minutes = (millis % 3_600_000) / 60_000
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    private fun calculateCurrentCaffeine(shots: List<CaffeineShot>, currentTime: Long): Double {
        return shots.sumOf { shot ->
            val hoursPassed = (currentTime - shot.timestamp).toDouble() / TimeUnit.HOURS.toMillis(1)
            if (hoursPassed < 0) 0.0 else shot.mg * (0.5).pow(hoursPassed / halfLifeHours)
        }
    }

    private fun getRecommendation(currentLevel: Double, exerciseMinutes: Int, hydrationLiters: Double): String {
        return when {
            hydrationLiters < 1.0 -> "Dehydration risk. Drink 500ml water now!"
            exerciseMinutes > 30 && currentLevel < 100 -> "Post-workout energy dip? A 40mg dose is safe."
            currentLevel < 50 -> "Time for a 20mg micro-dose!"
            else -> "Energy level optimal. Wait 2 hours."
        }
    }

    private fun getSleepImpact(currentLevel: Double, sleepMillis: Long): String {
        val hoursSleep = sleepMillis / 3_600_000
        return when {
            hoursSleep < 6 && currentLevel > 50 -> "Low sleep last night. Caution with high doses."
            currentLevel > 100 -> "High levels might disrupt sleep."
            else -> "Safe for a good night's rest."
        }
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Refresh -> refreshTrigger.tryEmit(Unit)
        }
    }
}
