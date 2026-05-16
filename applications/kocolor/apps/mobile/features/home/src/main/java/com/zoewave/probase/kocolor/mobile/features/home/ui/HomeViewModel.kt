package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import com.zoewave.probase.kocolor.db.data.ClothingDefaults
import com.zoewave.probase.kocolor.db.data.CosmeticDefaults
import com.zoewave.probase.kocolor.features.routines.data.RoutineDefaults
import com.zoewave.probase.kocolor.model.*
import com.zoewave.probase.features.health.core.WellnessCorrelationEngine
import com.zoewave.probase.features.health.core.SkinInsight
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import com.zoewave.probase.core.model.health.SleepSessionData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    val cosmeticsByGroup: Map<String, Int> = emptyMap(),
    val wellnessInsights: List<SkinInsight> = emptyList(),
    val lastNightSleepDuration: String? = null
)

sealed class HomeEvent {
    data class ToggleStep(val routine: BeautyRoutine, val stepId: String) : HomeEvent()
    data object RefreshTip : HomeEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fashionRepository: FashionRepository,
    private val routineDao: RoutineDao,
    private val cosmeticDao: CosmeticDao,
    private val clothingDao: ClothingDao,
    private val wellnessEngine: WellnessCorrelationEngine,
    private val healthSessionManager: HealthSessionManager
) : ViewModel() {

    private val _currentDate = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis)

    private val _beautyTip = MutableStateFlow(RoutineDefaults.dailyBeautyAdvice.random())

    init {
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
        _routines,
        cosmeticDao.getAllCosmetics(),
        clothingDao.getAllClothing(),
        _beautyTip,
        flow { emit(getHealthData()) }
    ) { array ->
        val profile = array[0] as FashionProfile?
        val routines = array[1] as List<RoutineEntity>
        val cosmetics = array[2] as List<CosmeticItemEntity>
        val clothing = array[3] as List<ClothingItemEntity>
        val tip = array[4] as String
        val healthData = array[5] as Pair<Float?, String?>

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val cosmeticsByGroup = cosmetics.groupBy { it.category.groupName }.mapValues { it.value.size }
        
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
            cosmeticsByGroup = cosmeticsByGroup,
            wellnessInsights = insights,
            lastNightSleepDuration = healthData.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private suspend fun getHealthData(): Pair<Float?, String?> {
        return try {
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
                _beautyTip.value = RoutineDefaults.dailyBeautyAdvice.random()
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
        category = category,
        colorHex = colorHex,
        shadeName = shadeName,
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
