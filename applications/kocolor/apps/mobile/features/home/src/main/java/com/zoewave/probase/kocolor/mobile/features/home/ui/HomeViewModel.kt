package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherCondition
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherUiState
import com.zoewave.probase.features.health.core.SkinInsight
import com.zoewave.probase.features.health.core.WellnessCorrelationEngine
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.db.data.ClothingDefaults
import com.zoewave.probase.kocolor.db.data.CosmeticDefaults
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import com.zoewave.probase.kocolor.features.routines.data.RoutineDefaults
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.FashionProfile
import com.zoewave.probase.kocolor.model.RoutineTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val fashionProfile: FashionProfile? = null,
    val morningRoutine: BeautyRoutine? = null,
    val eveningRoutine: BeautyRoutine? = null,
    val popularCosmetics: List<CosmeticItem> = emptyList(),
    val popularClothing: List<ClothingItem> = emptyList(),
    val isDaytime: Boolean = true,
    val isLoadingRoutines: Boolean = true,
    val beautyTip: String = "",
    val totalCosmetics: Int = 0,
    val totalClothing: Int = 0,
    val expiringCosmeticsCount: Int = 0,
    val totalVanityValue: Double = 0.0,
    val totalWardrobeValue: Double = 0.0,
    val cosmeticsByGroup: Map<String, Int> = emptyMap(),
    val clothingByCategory: Map<String, Int> = emptyMap(),
    val wellnessInsights: List<SkinInsight> = emptyList(),
    val lastNightSleepDuration: String? = null,
    val hydrationLiters: Double = 0.0,
    val hydrationGoalLiters: Double = 2.0,
    val isHealthPermissionGranted: Boolean = false,
    val weather: LayeredWeatherUiState? = null,
    val locationName: String? = null,
    val headerBackgroundUrl: String? = null,
    val savedSuggestions: List<com.zoewave.probase.kocolor.model.SavedAnalysis> = emptyList()
)

sealed class HomeEvent {
    data class ToggleStep(val routine: BeautyRoutine, val stepId: String) : HomeEvent()
    data object RefreshTip : HomeEvent()
    data class LogHydration(val volumeLiters: Double) : HomeEvent()
    data object RefreshWeather : HomeEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fashionRepository: FashionRepository,
    private val routineDao: RoutineDao,
    private val cosmeticDao: CosmeticDao,
    private val clothingDao: ClothingDao,
    private val wellnessEngine: WellnessCorrelationEngine,
    private val healthSessionManager: HealthSessionManager,
    private val weatherRepo: com.zoewave.probase.core.network.repository.weather.WeatherRepo,
    private val locationRepository: com.zoewave.probase.core.data.repository.travel.LocationRepository,
    private val koColorSettings: com.zoewave.probase.kocolor.db.KoColorSettings
) : ViewModel() {

    private val _currentDate = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis)

    private val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    private val isDaytime = hour in 6..17

    private val _beautyTip = MutableStateFlow(
        if (isDaytime) RoutineDefaults.morningAdvice.random() 
        else RoutineDefaults.eveningAdvice.random()
    )

    private val _weather = MutableStateFlow<LayeredWeatherUiState?>(null)
    private val _headerBackgroundUrl = MutableStateFlow<String?>(null)

    init {
        fetchWeather()
        viewModelScope.launch {
            cosmeticDao.getAllCosmetics().first().let {
                if (it.isEmpty()) {
                    initializeDefaultCosmetics()
                }
            }
        }
        viewModelScope.launch {
            clothingDao.getAllClothing().first().let {
                if (it.isEmpty()) {
                    initializeDefaultClothing()
                }
            }
        }
    }

    private suspend fun initializeDefaultCosmetics() {
        for (item in CosmeticDefaults.getDefaultCosmetics()) {
            cosmeticDao.insertCosmetic(item)
        }
    }

    private suspend fun initializeDefaultClothing() {
        for (item in ClothingDefaults.getDefaultClothing()) {
            clothingDao.insertClothing(item)
        }
    }

    private val _routines = _currentDate.flatMapLatest { date ->
        val startOfDay = date
        val endOfDay = date + 24 * 60 * 60 * 1000
        routineDao.getRoutinesForDay(startOfDay, endOfDay).onEach { entities ->
            if (entities.isEmpty()) initializeDay(date)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        fashionRepository.getProfile(),
        koColorSettings.hydrationGoalFlow,
        _routines,
        cosmeticDao.getAllCosmetics(),
        clothingDao.getAllClothing(),
        _beautyTip,
        _weather,
        _headerBackgroundUrl,
        healthSessionManager.availability.flatMapLatest { availability ->
            if (availability == HealthConnectClient.SDK_AVAILABLE) {
                flow {
                    val permissions = setOf(
                        HealthPermission.getReadPermission(SleepSessionRecord::class),
                        HealthPermission.getReadPermission(HydrationRecord::class),
                        HealthPermission.getWritePermission(HydrationRecord::class)
                    )
                    val hasPerms = healthSessionManager.hasAllPermissions(permissions)
                    if (hasPerms) {
                        emit(hasPerms to getHealthData())
                    } else {
                        emit(false to Triple(null as Float?, null as String?, 0.0))
                    }
                }
            } else {
                flowOf(false to Triple(null as Float?, null as String?, 0.0))
            }
        },
        fashionRepository.getSavedSuggestions()
    ) { array ->
        val profile = array[0] as FashionProfile?
        val hydrationGoal = array[1] as Double
        val routines = array[2] as List<RoutineEntity>
        val cosmetics = array[3] as List<CosmeticItemEntity>
        val clothing = array[4] as List<ClothingItemEntity>
        val tip = array[5] as String
        val weather = array[6] as LayeredWeatherUiState?
        val headerBg = array[7] as String?
        val healthInfo = array[8] as Pair<Boolean, Triple<Float?, String?, Double>>
        val (hasPerms, healthData) = healthInfo
        val savedSuggestions = array[9] as List<com.zoewave.probase.kocolor.model.SavedAnalysis>

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val cosmeticsByGroup = cosmetics.groupBy { it.macroCategory.displayName }.mapValues { it.value.size }
        val clothingByCategory = clothing.groupBy { it.category.name }.mapValues { it.value.size }

        val totalVanityValue = cosmetics.sumOf { it.price ?: 0.0 }
        val totalWardrobeValue = clothing.sumOf { it.price ?: 0.0 }

        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        val now = System.currentTimeMillis()
        val expiringCount = cosmetics.count { entity ->
            val item = entity.toModel()
            item.estimatedExpiry?.let { expiry ->
                (expiry - now) in 0..thirtyDaysInMillis
            } ?: false
        }
        
        val insights = wellnessEngine.analyzeTriggers(
            sleepHours = healthData.first ?: 8f,
            sugarIntake = "Medium", // Placeholder for now
            stressLevel = 5 // Placeholder for now
        )

        HomeUiState(
            fashionProfile = profile,
            morningRoutine = routines.find { it.time == RoutineTime.MORNING }?.toModel(),
            eveningRoutine = routines.find { it.time == RoutineTime.EVENING }?.toModel(),
            popularCosmetics = cosmetics.sortedByDescending { it.timestamp }.take(5).map { it.toModel() },
            popularClothing = clothing.sortedByDescending { it.timestamp }.take(5).map { it.toModel() },
            isDaytime = hour in 6..17,
            isLoadingRoutines = routines.isEmpty(),
            beautyTip = tip,
            totalCosmetics = cosmetics.size,
            totalClothing = clothing.size,
            totalVanityValue = totalVanityValue,
            totalWardrobeValue = totalWardrobeValue,
            expiringCosmeticsCount = expiringCount,
            cosmeticsByGroup = cosmeticsByGroup,
            clothingByCategory = clothingByCategory,
            wellnessInsights = insights,
            lastNightSleepDuration = healthData.second,
            hydrationLiters = healthData.third,
            hydrationGoalLiters = hydrationGoal,
            isHealthPermissionGranted = hasPerms,
            weather = weather,
            locationName = weather?.locationName,
            headerBackgroundUrl = headerBg,
            savedSuggestions = savedSuggestions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private suspend fun getHealthData(): Triple<Float?, String?, Double> {
        val now = Instant.now()
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()

        val sleepData = try {
            val sleepSessions = healthSessionManager.readSleepSessions()
            val lastNight = sleepSessions.firstOrNull()
            if (lastNight != null) {
                val hours = (lastNight.duration?.toMinutes() ?: 0L) / 60f
                val durationStr = "${(hours).toInt()}h ${lastNight.duration?.toMinutes()?.rem(60)}m"
                hours to durationStr
            } else {
                null to null
            }
        } catch (e: Exception) {
            null to null
        }

        val hydration = try {
            healthSessionManager.readTotalHydration(startOfDay, now)?.inLiters ?: 0.0
        } catch (e: Exception) {
            0.0
        }

        return Triple(sleepData.first, sleepData.second, hydration)
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            try {
                // 1. Attempt to get GPS coordinates with a 5s timeout
                val latLng = kotlinx.coroutines.withTimeoutOrNull(5000) {
                    locationRepository.updateLocation()
                    locationRepository.currentLocation.first { it != null }
                }
                
                if (latLng != null) {
                    // 2. Fetch weather by real coords
                    val response = weatherRepo.openCurrentWeatherByCoords(latLng.latitude, latLng.longitude)
                    
                    // 3. Get environmental context (UV, humidity)
                    val envContext = weatherRepo.getEnvironmentalContext(latLng.latitude, latLng.longitude)

                    updateWeatherState(response, envContext)
                } else {
                    // 4. Fallback to Santa Barbara if GPS fails or times out
                    android.util.Log.d("HomeViewModel", "GPS timeout/unavailable. Falling back to Santa Barbara.")
                    val fallbackCity = "Santa Barbara, US"
                    val response = weatherRepo.openCurrentWeatherByCity(fallbackCity)
                    
                    val envContext = response?.coord?.let { 
                        weatherRepo.getEnvironmentalContext(it.lat, it.lon)
                    }
                    updateWeatherState(response, envContext)
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error fetching weather", e)
            }
        }
    }

    private fun updateWeatherState(
        response: com.zoewave.probase.core.model.weather.OpenWeatherResponse?,
        envContext: com.zoewave.probase.core.model.weather.EnvironmentalContext?
    ) {
        if (response != null && envContext != null) {
            val conditions = mutableListOf<LayeredWeatherCondition>()
            val main = response.weather.firstOrNull()?.main ?: ""
            when {
                main.contains("Cloud", true) -> conditions.add(LayeredWeatherCondition.CLOUDY)
                main.contains("Rain", true) -> conditions.add(LayeredWeatherCondition.RAINY)
                main.contains("Thunder", true) -> conditions.add(LayeredWeatherCondition.THUNDER)
                else -> conditions.add(LayeredWeatherCondition.SUNNY)
            }
            if (response.wind.speed > 5.0) conditions.add(LayeredWeatherCondition.WINDY)
            
            _weather.value = LayeredWeatherUiState(
                temperature = response.main.temp,
                uvIndex = envContext.uvIndex,
                conditions = conditions,
                locationName = response.name
            )

            // Dynamic Unsplash Background
            val weatherKeyword = when {
                conditions.contains(LayeredWeatherCondition.THUNDER) -> "storm"
                conditions.contains(LayeredWeatherCondition.RAINY) -> "rainy"
                conditions.contains(LayeredWeatherCondition.CLOUDY) -> "cloudy"
                conditions.contains(LayeredWeatherCondition.SUNNY) -> "sunny"
                else -> "nature"
            }
            // Using Source Unsplash redirect for efficiency as requested
            // Note: In production we'd use Unsplash API and cached IDs
            _headerBackgroundUrl.value = "https://images.unsplash.com/featured/?skincare,weather,$weatherKeyword"

            // Environmental Trigger Logic
            if (envContext.uvIndex > 3.0) {
                _beautyTip.value = "☀️ High UV detected. Prioritize SPF in your ritual today."
            } else if (envContext.humidity < 30.0) {
                _beautyTip.value = "💧 Low humidity. Use a humectant to retain moisture."
            }
        }
    }

    private fun initializeDay(date: Long) {
        viewModelScope.launch {
            routineDao.insertRoutine(RoutineEntity(
                title = "morning beautiful routine",
                time = RoutineTime.MORNING,
                steps = RoutineDefaults.getMorningRoutine(),
                date = date
            ))
            routineDao.insertRoutine(RoutineEntity(
                title = "Evening Routine",
                time = RoutineTime.EVENING,
                steps = RoutineDefaults.getEveningRoutine(),
                date = date
            ))
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ToggleStep -> toggleStep(event.routine, event.stepId)
            HomeEvent.RefreshTip -> {
                _beautyTip.value = if (isDaytime) RoutineDefaults.morningAdvice.random() 
                else RoutineDefaults.eveningAdvice.random()
            }
            is HomeEvent.LogHydration -> {
                viewModelScope.launch {
                    healthSessionManager.insertHydration(event.volumeLiters)
                }
            }
            HomeEvent.RefreshWeather -> fetchWeather()
        }
    }

    private fun toggleStep(routine: BeautyRoutine, stepId: String) {
        viewModelScope.launch {
            val updatedSteps = routine.steps.map {
                if (it.id == stepId) it.copy(isCompleted = !it.isCompleted) else it
            }
            routineDao.updateRoutine(RoutineEntity(
                id = routine.id,
                title = routine.title,
                time = routine.time,
                steps = updatedSteps,
                date = routine.date
            ))
        }
    }

    private fun RoutineEntity.toModel() = BeautyRoutine(
        id = this.id,
        title = this.title,
        time = this.time,
        steps = this.steps,
        date = this.date
    )

    private fun CosmeticItemEntity.toModel() = CosmeticItem(
        id = id,
        name = name,
        brand = brand,
        macroCategory = macroCategory,
        microCategory = microCategory,
        formulation = formulation,
        chemistryBase = chemistryBase,
        finish = finish,
        coverage = coverage,
        colorHex = colorHex,
        shadeName = shadeName,
        imageUrl = imageUrl,
        notes = notes,
        timestamp = timestamp,
        batchCode = batchCode,
        openedDate = openedDate,
        paoMonths = paoMonths,
        expiryDate = expiryDate,
        price = price,
        volume = volume,
        isOpened = isOpened,
        isFinished = isFinished,
        isArchived = isArchived,
        usageCount = usageCount
    )

    private fun ClothingItemEntity.toModel() = ClothingItem(
        id = id,
        name = name,
        brand = brand,
        category = category,
        colorHex = colorHex,
        size = size,
        material = material,
        imageUrl = imageUrl,
        notes = notes,
        timestamp = timestamp
    )
}
