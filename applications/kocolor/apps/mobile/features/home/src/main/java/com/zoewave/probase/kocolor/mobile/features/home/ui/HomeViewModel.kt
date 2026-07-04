package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.weather.AtmosphericRepository
import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.core.model.ritual.RoutineTime
import com.zoewave.probase.core.model.weather.AtmosphericState
import com.zoewave.probase.features.health.core.SkinInsight
import com.zoewave.probase.features.health.core.domain.GetActiveRitualUseCase
import com.zoewave.probase.features.health.core.domain.GetHealthSummaryUseCase
import com.zoewave.probase.features.health.core.domain.HealthSummary
import com.zoewave.probase.features.health.core.domain.LogHydrationUseCase
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherMapper
import com.zoewave.probase.features.weather.ui.components.layered.LayeredWeatherUiState
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.db.KoColorSettings
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import com.zoewave.probase.kocolor.features.routines.data.RoutineDefaults
import com.zoewave.probase.kocolor.features.store.ui.StoreUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    val storeUiState: StoreUiState = StoreUiState(),
    val savedSuggestions: List<com.zoewave.probase.core.model.ritual.SavedAnalysis> = emptyList()
)

sealed class HomeEvent {
    data class ToggleStep(val routine: BeautyRoutine, val stepId: String) : HomeEvent()
    data object RefreshTip : HomeEvent()
    data class LogHydration(val volumeLiters: Double) : HomeEvent()
    data object RefreshWeather : HomeEvent()
    data object ToggleStoreExpansion : HomeEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fashionRepository: FashionRepository,
    private val routineDao: RoutineDao,
    private val cosmeticDao: CosmeticDao,
    private val clothingDao: ClothingDao,
    private val getHealthSummaryUseCase: GetHealthSummaryUseCase,
    private val getActiveRitualUseCase: GetActiveRitualUseCase,
    private val logHydrationUseCase: LogHydrationUseCase,
    private val atmosphericRepository: AtmosphericRepository,
    private val koColorSettings: KoColorSettings
) : ViewModel() {

    private val _currentDate = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis)

    private val _beautyTip = MutableStateFlow("")
    private val _isStoreExpanded = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            atmosphericRepository.fetchWeatherIfNeeded()
        }
        initializeTip()
    }

    private fun initializeTip() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        _beautyTip.value = if (hour in 5..17) RoutineDefaults.morningAdvice.random() 
        else RoutineDefaults.eveningAdvice.random()
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
        _isStoreExpanded,
        atmosphericRepository.atmosphericState,
        getHealthSummaryUseCase(),
        fashionRepository.getSavedSuggestions()
    ) { array ->
        val profile = array[0] as FashionProfile?
        val hydrationGoal = array[1] as Double
        val tempUnit = array[2] as String
        val routineEntities = array[3] as List<RoutineEntity>
        val cosmetics = array[4] as List<CosmeticItemEntity>
        val clothing = array[5] as List<ClothingItemEntity>
        val tip = array[6] as String
        val isStoreExpanded = array[7] as Boolean
        val atmosphericState = array[8] as AtmosphericState
        val healthSummary = array[9] as HealthSummary
        val savedSuggestions = array[10] as List<com.zoewave.probase.core.model.ritual.SavedAnalysis>

        val routines = routineEntities.map { it.toModel() }
        val activeRitual = getActiveRitualUseCase(routines)
        
        val weather = atmosphericState.weather?.let { resp ->
            atmosphericState.environmentalContext?.let { ctx ->
                LayeredWeatherMapper.mapToUiState(resp, ctx, atmosphericState.isFallback)
            }
        }

        // cosmetics.sortedByDescending { it.timestamp }.take(5).map { it.toModel() }
        val popularCosmetics = cosmetics
            .sortedByDescending { it.timestamp }
            .take(5)
            .map { it.toModel()}


        val popularClothing = clothing
            .sortedByDescending { it.timestamp }
            .take(5)
            .map { it.toModel() }

        val cosmeticsByGroup = cosmetics.groupBy { it.macroCategory.displayName }.mapValues { it.value.size }
        val clothingByCategory = clothing.groupBy { it.category.name }.mapValues { it.value.size }

        val totalVanityValue = cosmetics.sumOf { it.price ?: 0.0 }
        val totalWardrobeValue = clothing.sumOf { it.price ?: 0.0 }

        val now = System.currentTimeMillis()
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        val expiringCount = cosmetics.count { entity ->
            val item = entity.toModel()
            item.estimatedExpiry?.let { expiry -> (expiry - now) in 0..thirtyDaysInMillis } ?: false
        }

        val processedWeather = weather?.let {
            if (tempUnit == "FAHRENHEIT") it.copy(temperature = (it.temperature * 9 / 5) + 32) else it
        }

        HomeUiState(
            fashionProfile = profile,
            morningRoutine = routines.find { it.time == RoutineTime.MORNING },
            mealsRoutine = routines.find { it.time == RoutineTime.MEALS },
            eveningRoutine = routines.find { it.time == RoutineTime.EVENING },
            currentRoutine = activeRitual.routine,
            currentRoutineTitle = activeRitual.title,
            currentRoutineDescription = activeRitual.description,
            popularCosmetics = popularCosmetics,
            popularClothing = popularClothing,
            isDaytime = activeRitual.isDaytime,
            isLoadingRoutines = routineEntities.isEmpty(),
            beautyTip = tip,
            totalCosmetics = cosmetics.size,
            totalClothing = clothing.size,
            totalVanityValue = totalVanityValue,
            totalWardrobeValue = totalWardrobeValue,
            expiringCosmeticsCount = expiringCount,
            cosmeticsByGroup = cosmeticsByGroup,
            clothingByCategory = clothingByCategory,
            wellnessInsights = healthSummary.insights,
            lastNightSleepDuration = healthSummary.sleepDurationLabel,
            hydrationLiters = healthSummary.hydrationLiters,
            hydrationGoalLiters = hydrationGoal,
            isHealthPermissionGranted = healthSummary.hasPermissions,
            weather = processedWeather,
            locationName = weather?.locationName,
            temperatureUnit = tempUnit,
            storeUiState = StoreUiState(isExpanded = isStoreExpanded),
            savedSuggestions = savedSuggestions,
            isLocationFallback = weather?.locationName == "Location could not be found"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

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
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                _beautyTip.value = if (hour in 5..17) RoutineDefaults.morningAdvice.random() 
                else RoutineDefaults.eveningAdvice.random()
            }
            is HomeEvent.LogHydration -> {
                viewModelScope.launch {
                    logHydrationUseCase(event.volumeLiters)
                }
            }
            HomeEvent.RefreshWeather -> {
                viewModelScope.launch {
                    atmosphericRepository.refreshWeather()
                }
            }
            HomeEvent.ToggleStoreExpansion -> {
                _isStoreExpanded.value = !_isStoreExpanded.value
            }
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
