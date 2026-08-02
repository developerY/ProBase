package com.zoewave.probase.kocolor.mobile.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.KoColorSettings
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import com.zoewave.probase.kocolor.features.settings.domain.seeder.VaultSeeder
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isAiExpanded: Boolean = false,
    val isAboutExpanded: Boolean = false,
    val isThemeExpanded: Boolean = false,
    val isPaletteExpanded: Boolean = false,
    val isHealthExpanded: Boolean = false,
    val isAppSettingsExpanded: Boolean = false,
    val isHydrationExpanded: Boolean = false,
    val currentTheme: String = "SYSTEM",
    val currentPalette: String = "CLASSIC",
    val tempUnit: String = "CELSIUS",
    val hydrationGoal: Double = 2.7,
    val seedingState: SeedingState = SeedingState.Idle
)

sealed class SettingsEvent {
    data class OnAiExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnAboutExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnThemeExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnPaletteExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnHealthExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnAppSettingsExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnHydrationExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnThemeSelected(val theme: String) : SettingsEvent()
    data class OnPaletteSelected(val palette: String) : SettingsEvent()
    data class OnTempUnitChanged(val unit: String) : SettingsEvent()
    data class OnHydrationGoalChanged(val goal: Double) : SettingsEvent()
    data object OnResetHydrationProgress : SettingsEvent()
    data object OnGenerateSampleData : SettingsEvent()
    data object OnIngestStarterPack : SettingsEvent()
    data class InitializeWithSection(val section: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val koSettings: KoColorSettings,
    private val healthSessionManager: HealthSessionManager,
    private val vaultSeeder: VaultSeeder,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val wardrobeRepository: WardrobeRepository
) : ViewModel() {

    private val _expandState = MutableStateFlow(
        listOf(false, false, false, false, false, false, false) // AI, About, Theme, Palette, Health, AppSettings, Hydration
    )

    private val _seedingState = MutableStateFlow<SeedingState>(SeedingState.Idle)

    val uiState: StateFlow<SettingsUiState> = combine(
        _expandState,
        koSettings.appThemeFlow,
        koSettings.colorPaletteFlow,
        koSettings.temperatureUnitFlow,
        combine(koSettings.hydrationGoalFlow, _seedingState) { h, s -> h to s }
    ) { expands, theme, palette, tempUnit, extra ->
        val (hydrationGoal, seedingState) = extra
        SettingsUiState(
            isAiExpanded = expands[0] as Boolean,
            isAboutExpanded = expands[1] as Boolean,
            isThemeExpanded = expands[2] as Boolean,
            isPaletteExpanded = expands[3] as Boolean,
            isHealthExpanded = expands[4] as Boolean,
            isAppSettingsExpanded = expands[5] as Boolean,
            isHydrationExpanded = expands[6] as Boolean,
            currentTheme = theme,
            currentPalette = palette,
            tempUnit = tempUnit,
            hydrationGoal = hydrationGoal,
            seedingState = seedingState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnAiExpandedToggled -> {
                _expandState.value = _expandState.value.toMutableList().apply { this[0] = event.expanded }
            }
            is SettingsEvent.OnAboutExpandedToggled -> {
                _expandState.value = _expandState.value.toMutableList().apply { this[1] = event.expanded }
            }
            is SettingsEvent.OnThemeExpandedToggled -> {
                _expandState.value = _expandState.value.toMutableList().apply { this[2] = event.expanded }
            }
            is SettingsEvent.OnPaletteExpandedToggled -> {
                _expandState.value = _expandState.value.toMutableList().apply { this[3] = event.expanded }
            }
            is SettingsEvent.OnHealthExpandedToggled -> {
                _expandState.value = _expandState.value.toMutableList().apply { this[4] = event.expanded }
            }
            is SettingsEvent.OnAppSettingsExpandedToggled -> {
                _expandState.value = _expandState.value.toMutableList().apply { this[5] = event.expanded }
            }
            is SettingsEvent.OnHydrationExpandedToggled -> {
                _expandState.value = _expandState.value.toMutableList().apply { this[6] = event.expanded }
            }
            is SettingsEvent.OnThemeSelected -> {
                viewModelScope.launch {
                    koSettings.saveAppTheme(event.theme)
                }
            }
            is SettingsEvent.OnPaletteSelected -> {
                viewModelScope.launch {
                    koSettings.saveColorPalette(event.palette)
                }
            }
            is SettingsEvent.OnTempUnitChanged -> {
                viewModelScope.launch {
                    koSettings.saveTemperatureUnit(event.unit)
                }
            }
            is SettingsEvent.OnHydrationGoalChanged -> {
                viewModelScope.launch {
                    koSettings.saveHydrationGoal(event.goal)
                }
            }
            SettingsEvent.OnResetHydrationProgress -> {
                viewModelScope.launch {
                    healthSessionManager.deleteTodayHydration()
                }
            }
            SettingsEvent.OnGenerateSampleData -> {
                triggerDatabaseSeed()
            }
            SettingsEvent.OnIngestStarterPack -> {
                ingestStarterPack()
            }
            is SettingsEvent.InitializeWithSection -> {
                if (event.section == "Hydration") {
                    _expandState.value = listOf(false, false, false, false, false, true, true)
                }
            }
        }
    }

    private fun triggerDatabaseSeed() {
        viewModelScope.launch {
            _seedingState.value = SeedingState.Loading
            vaultSeeder.wipeAndSeedDatabase()
                .onSuccess {
                    _seedingState.value = SeedingState.Success
                }
                .onFailure { error ->
                    _seedingState.value = SeedingState.Error(error.localizedMessage ?: "Unknown Error")
                }
        }
    }

    private fun ingestStarterPack() {
        viewModelScope.launch {
            _seedingState.value = SeedingState.Loading
            
            val cosmeticResult = cosmeticRepository.ingestStarterPack()
            val wardrobeResult = wardrobeRepository.ingestStarterPack()
            
            if (cosmeticResult.isSuccess && wardrobeResult.isSuccess) {
                _seedingState.value = SeedingState.Success
            } else {
                val error = cosmeticResult.exceptionOrNull()?.localizedMessage 
                    ?: wardrobeResult.exceptionOrNull()?.localizedMessage 
                    ?: "Ingestion Failed"
                _seedingState.value = SeedingState.Error(error)
            }
        }
    }
}
