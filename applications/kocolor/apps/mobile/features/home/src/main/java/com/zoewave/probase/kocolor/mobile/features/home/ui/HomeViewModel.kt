package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherMapper
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherUiState
import com.zoewave.probase.features.weather.ui.components.layered.WeatherAdvice
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
import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.core.model.ritual.RoutineTime
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
    val mealsRoutine: BeautyRoutine? = null,
    val eveningRoutine: BeautyRoutine? = null,
    val currentRoutine: BeautyRoutine? = null,
    val currentRoutineTitle: String? = null,
    val currentRoutineDescription: String? = null,
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
    val isLocationFallback: Boolean = false,
    val temperatureUnit: String = "CELSIUS",
    val headerBackgroundUrl: String? = null,
    val savedSuggestions: List<com.zoewave.probase.core.model.ritual.SavedAnalysis> = emptyList()
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
    private val activeRoutineTime = when {
        hour in 5..9 -> RoutineTime.MORNING
        hour in 10..19 -> RoutineTime.MEALS
        else -> RoutineTime.EVENING
    }
    
    private val isDaytime = activeRoutineTime != RoutineTime.EVENING

    private val _beautyTip = MutableStateFlow(
        if (activeRoutineTime == RoutineTime.MORNING) RoutineDefaults.morningAdvice.random() 
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
            if (entities.size < 3) {
                initializeDay(date, entities)
            } else {
                patchRoutineMetadata(entities)
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        fashionRepository.getProfile(),
        koColorSettings.hydrationGoalFlow,
        koColorSettings.temperatureUnitFlow,
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
        val tempUnit = array[2] as String
        val routines = array[3] as List<RoutineEntity>
        val cosmetics = array[4] as List<CosmeticItemEntity>
        val clothing = array[5] as List<ClothingItemEntity>
        val tip = array[6] as String
        val weather = array[7] as LayeredWeatherUiState?
        val headerBg = array[8] as String?
        val healthInfo = array[9] as Pair<Boolean, Triple<Float?, String?, Double>>
        val (hasPerms, healthData) = healthInfo
        val savedSuggestions = array[10] as List<com.zoewave.probase.core.model.ritual.SavedAnalysis>

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

        val processedWeather = weather?.let {
            if (tempUnit == "FAHRENHEIT") {
                it.copy(temperature = (it.temperature * 9 / 5) + 32)
            } else it
        }

        val morning = routines.find { it.time == RoutineTime.MORNING }?.toModel()
        val meals = routines.find { it.time == RoutineTime.MEALS }?.toModel()
        val evening = routines.find { it.time == RoutineTime.EVENING }?.toModel()

        val (currentRoutine, currentTitle, currentDesc) = when {
            hour in 5..9 -> Triple(morning, "Morning Ritual", "Prepare for a balanced day ahead.")
            hour in 10..19 -> Triple(meals, "Meals Ritual", "Nourish your metabolism with precise biochemical timing.")
            else -> Triple(evening, "Evening Ritual", "Every step is an act of self-love.")
        }

        HomeUiState(
            fashionProfile = profile,
            morningRoutine = morning,
            mealsRoutine = meals,
            eveningRoutine = evening,
            currentRoutine = currentRoutine,
            currentRoutineTitle = currentTitle,
            currentRoutineDescription = currentDesc,
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
            weather = processedWeather,
            locationName = weather?.locationName,
            temperatureUnit = tempUnit,
            headerBackgroundUrl = headerBg,
            savedSuggestions = savedSuggestions,
            isLocationFallback = weather?.locationName == "Location could not be found"
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

                    updateWeatherState(response, envContext, isFallback = false)
                } else {
                    // 4. Fallback to Santa Barbara if GPS fails or times out
                    android.util.Log.d("HomeViewModel", "GPS timeout/unavailable. Falling back to Santa Barbara.")
                    val fallbackCity = "Santa Barbara, US"
                    val response = weatherRepo.openCurrentWeatherByCity(fallbackCity)
                    
                    val envContext = response?.coord?.let { 
                        weatherRepo.getEnvironmentalContext(it.lat, it.lon)
                    }
                    updateWeatherState(response, envContext, isFallback = true)
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error fetching weather", e)
            }
        }
    }

    private fun updateWeatherState(
        response: com.zoewave.probase.core.model.weather.OpenWeatherResponse?,
        envContext: com.zoewave.probase.core.model.weather.EnvironmentalContext?,
        isFallback: Boolean
    ) {
        if (response != null && envContext != null) {
            _weather.value = LayeredWeatherMapper.mapToUiState(response, envContext, isFallback)

            // Environmental Trigger Logic
            WeatherAdvice.getBeautyAdvice(envContext)?.let { advice ->
                _beautyTip.value = advice
            }
        }
    }

    private fun initializeDay(date: Long, existing: List<RoutineEntity> = emptyList()) {
        val existingTimes = existing.map { it.time }.toSet()
        viewModelScope.launch {
            if (RoutineTime.MORNING !in existingTimes) {
                routineDao.insertRoutine(RoutineEntity(
                    title = "morning beautiful routine",
                    time = RoutineTime.MORNING,
                    steps = RoutineDefaults.getMorningRoutine(),
                    date = date
                ))
            }
            if (RoutineTime.MEALS !in existingTimes) {
                routineDao.insertRoutine(RoutineEntity(
                    title = "Meals Routine",
                    time = RoutineTime.MEALS,
                    steps = RoutineDefaults.getMealsRoutine(),
                    date = date
                ))
            }
            if (RoutineTime.EVENING !in existingTimes) {
                routineDao.insertRoutine(RoutineEntity(
                    title = "Evening Routine",
                    time = RoutineTime.EVENING,
                    steps = RoutineDefaults.getEveningRoutine(),
                    date = date
                ))
            }
        }
    }

    private fun patchRoutineMetadata(entities: List<RoutineEntity>) {
        viewModelScope.launch {
            entities.forEach { entity ->
                // Identify mislabeled meals routines: 5 steps but labeled as Evening
                if (entity.time == RoutineTime.EVENING && entity.steps.size == 5) {
                    routineDao.updateRoutine(entity.copy(
                        title = "Meals Routine",
                        time = RoutineTime.MEALS
                    ))
                }
            }
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
