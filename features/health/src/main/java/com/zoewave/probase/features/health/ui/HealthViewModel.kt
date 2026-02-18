package com.zoewave.probase.features.health.ui

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import com.zoewave.probase.features.health.domain.HealthRideRequest
import com.zoewave.probase.features.health.domain.SyncRideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

// 1. Define Side Effects
sealed interface HealthSideEffect {
    data class LaunchPermissions(val permissions: Set<String>) : HealthSideEffect
    data class BikeRideSyncedToHealth(val rideId: String, val healthConnectId: String, val stats: String) : HealthSideEffect
    data object OpenHealthConnectSettings : HealthSideEffect
}

@HiltViewModel
class HealthViewModel @Inject constructor(
    val healthSessionManager: HealthSessionManager,
    private val syncRideUseCase: SyncRideUseCase
) : ViewModel() {

    // 2. State & Events
    private val _uiState = MutableStateFlow<HealthUiState>(HealthUiState.Uninitialized)
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<HealthSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    // 3. Define Permissions
    val permissions = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class)
    )

    // Note: Acceptable for specific UI toggles, though Flow is preferred for data
    var backgroundReadAvailable = mutableStateOf(false)
        private set
    var backgroundReadGranted = mutableStateOf(false)

    // 4. Track Synced IDs (Reactive)
    val syncedIds: StateFlow<Set<String>> = uiState
        .map { state ->
            (state as? HealthUiState.Success)
                ?.sessions
                ?.mapNotNull { it.metadata.clientRecordId }
                ?.toSet()
                .orEmpty()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    // 5. Main Event Handler
    fun onEvent(event: HealthEvent) {
        when (event) {
            is HealthEvent.LoadHealthData,
            is HealthEvent.Retry -> initialLoad()
            is HealthEvent.RequestPermissions -> requestPermissionsOnClick()
            is HealthEvent.Insert -> insertBikeRideSessionAndEmitEffect(event)
            is HealthEvent.DeleteAll -> delData()
            is HealthEvent.ReadAll -> readAllData()
            is HealthEvent.WriteTestRide -> writeTestCityRide()
            is HealthEvent.ManagePermissions -> {
                viewModelScope.launch {
                    _sideEffect.emit(HealthSideEffect.OpenHealthConnectSettings)
                }
            }
        }
    }

    // --- Private Actions ---

    private fun requestPermissionsOnClick() {
        viewModelScope.launch {
            val hasPermissions = healthSessionManager.hasAllPermissions(permissions)
            if (!hasPermissions) {
                _sideEffect.emit(HealthSideEffect.LaunchPermissions(permissions))
            } else {
                initialLoad()
            }
        }
    }

    private fun insertBikeRideSessionAndEmitEffect(event: HealthEvent.Insert) {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                try {
                    val clientRideId = healthSessionManager.insertBikeRideWithAssociatedData(
                        event.rideId,
                        event.records
                    )

                    // Calculate stats for the alert
                    val totalDist = event.records.filterIsInstance<DistanceRecord>()
                        .sumOf { it.distance.inKilometers }
                    val totalCals = event.records.filterIsInstance<TotalCaloriesBurnedRecord>()
                        .sumOf { it.energy.inKilocalories }
                    val statsMsg = "Distance: %.2f km\nEnergy: %.0f kcal".format(totalDist, totalCals)

                    _sideEffect.emit(
                        HealthSideEffect.BikeRideSyncedToHealth(
                            rideId = clientRideId,
                            healthConnectId = clientRideId,
                            stats = statsMsg
                        )
                    )
                    // Refresh data after insert
                    initialLoad()
                } catch (hcException: Exception) {
                    Log.e("HealthViewModel", "Error during Health Connect sync", hcException)
                    _uiState.value = HealthUiState.Error("Sync failed: ${hcException.message}")
                }
            }
        }
    }

    private fun initialLoad() {
        _uiState.value = HealthUiState.Loading
        viewModelScope.launch {
            if (healthSessionManager.availability.value != HealthConnectClient.SDK_AVAILABLE) {
                _uiState.value = HealthUiState.Error("Health Connect SDK not available.")
                return@launch
            }
            try {
                if (healthSessionManager.hasAllPermissions(permissions)) {
                    val sessions = readSessionInputs()

                    // Define the range
                    val end = Instant.now()
                    val start = ZonedDateTime.now().minusDays(7).truncatedTo(ChronoUnit.DAYS).toInstant()

                    // 1. Fetch Steps
                    val stepsMap = healthSessionManager.readSteps(start, end)
                        .groupBy { it.startTime.atZone(ZoneId.systemDefault()).toLocalDate().toString() }
                        .mapValues { entry -> entry.value.sumOf { it.count } }

                    // 2. Fetch Distance (Meters)
                    val distMap = healthSessionManager.readDistance(start, end)
                        .groupBy { it.startTime.atZone(ZoneId.systemDefault()).toLocalDate().toString() }
                        .mapValues { entry -> entry.value.sumOf { it.distance.inMeters } }

                    // 3. Fetch Calories (Kcal)
                    val calMap = healthSessionManager.readTotalCalories(start, end)
                        .groupBy { it.startTime.atZone(ZoneId.systemDefault()).toLocalDate().toString() }
                        .mapValues { entry -> entry.value.sumOf { it.energy.inKilocalories } }

                    _uiState.value = HealthUiState.Success(
                        sessions = sessions,
                        weeklySteps = stepsMap,
                        weeklyDistance = distMap,
                        weeklyCalories = calMap
                    )

                    observeHealthConnectChanges()
                } else {
                    _uiState.value = HealthUiState.PermissionsRequired("Displaying data requires permissions.")
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Failed to perform initial load", e)
                _uiState.value = HealthUiState.Error("Failed to read health data: ${e.message}")
            }
        }
    }

    private suspend fun readSessionInputs(): List<ExerciseSessionRecord> {
        val sevenDaysAgo = ZonedDateTime.now().minusDays(7).truncatedTo(ChronoUnit.DAYS)
        val now = Instant.now()
        return healthSessionManager.readExerciseSessions(sevenDaysAgo.toInstant(), now)
    }

    private suspend fun tryWithPermissionsCheck(block: suspend () -> Unit) {
        if (healthSessionManager.availability.value != HealthConnectClient.SDK_AVAILABLE) {
            _uiState.value = HealthUiState.Error("Health Connect SDK not available.")
            return
        }
        if (healthSessionManager.hasAllPermissions(permissions)) {
            try {
                block()
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Action failed", e)
                _uiState.value = HealthUiState.Error("Action failed: ${e.message}")
            }
        } else {
            _sideEffect.emit(HealthSideEffect.LaunchPermissions(permissions))
            _uiState.value = HealthUiState.PermissionsRequired("Action requires Health Connect permissions.")
        }
    }

    private fun writeTestCityRide() {
        viewModelScope.launch {
            // 1. Create Dummy Data (A 25-minute city commute)
            val end = System.currentTimeMillis()
            val start = end - (25 * 60 * 1000) // 25 mins ago

            val dummyRide = HealthRideRequest(
                id = UUID.randomUUID().toString(),
                startEpochMillis = start,
                endEpochMillis = end,
                distanceMeters = 4500.0, // 4.5 km
                caloriesKcal = 210.0,    // 210 kcal
                title = "Test City Ride \uD83D\uDEB4", // 🚴
                notes = "Simulated ride created via Debug Menu",
                avgHeartRate = 115,
                maxHeartRate = 130
            )

            // 2. Convert to Health Connect Records
            val records = syncRideUseCase(dummyRide)

            // 3. Insert into Health Connect
            tryWithPermissionsCheck {
                healthSessionManager.insertRecords(records)
                onEvent(HealthEvent.LoadHealthData)
            }
        }
    }

    private var isObservingChanges = false
    private fun observeHealthConnectChanges() {
        if (isObservingChanges || healthSessionManager.availability.value != HealthConnectClient.SDK_AVAILABLE) return
        viewModelScope.launch {
            isObservingChanges = true
            try {
                val token = healthSessionManager.getChangesToken(
                    setOf(ExerciseSessionRecord::class, StepsRecord::class)
                )
                healthSessionManager.getChanges(token)
                    .catch { isObservingChanges = false }
                    .filterIsInstance<HealthSessionManager.ChangesMessage.ChangeList>()
                    .collect {
                        initialLoad()
                    }
            } catch (e: Exception) {
                isObservingChanges = false
            }
        }
    }

    private fun readAllData() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                healthSessionManager.logAllHealthData()
            }
        }
    }

    private fun delData() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                healthSessionManager.deleteAllSessionData()
                initialLoad()
            }
        }
    }
}