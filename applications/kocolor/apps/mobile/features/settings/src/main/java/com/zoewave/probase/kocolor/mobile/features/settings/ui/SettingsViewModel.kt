package com.zoewave.probase.kocolor.mobile.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.KoColorSettings
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import com.zoewave.probase.core.model.ritual.*
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
    val hydrationGoal: Double = 2.7
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
    data class InitializeWithSection(val section: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val koSettings: KoColorSettings,
    private val cosmeticDao: CosmeticDao,
    private val clothingDao: ClothingDao,
    private val healthSessionManager: HealthSessionManager
) : ViewModel() {

    private val _expandState = MutableStateFlow(
        listOf(false, false, false, false, false, false, false) // AI, About, Theme, Palette, Health, AppSettings, Hydration
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        _expandState,
        koSettings.appThemeFlow,
        koSettings.colorPaletteFlow,
        koSettings.temperatureUnitFlow,
        koSettings.hydrationGoalFlow
    ) { expands, theme, palette, tempUnit, hydrationGoal ->
        SettingsUiState(
            isAiExpanded = expands[0],
            isAboutExpanded = expands[1],
            isThemeExpanded = expands[2],
            isPaletteExpanded = expands[3],
            isHealthExpanded = expands[4],
            isAppSettingsExpanded = expands[5],
            isHydrationExpanded = expands[6],
            currentTheme = theme,
            currentPalette = palette,
            tempUnit = tempUnit,
            hydrationGoal = hydrationGoal
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
                generateSampleData()
            }
            is SettingsEvent.InitializeWithSection -> {
                if (event.section == "Hydration") {
                    _expandState.value = listOf(false, false, false, false, false, true, true)
                }
            }
        }
    }

    private fun generateSampleData() {
        viewModelScope.launch {
            // 1. Generate 10 High-Fidelity Cosmetics
            val cosmeticBrands = listOf("Chanel", "Dior", "Fenty Beauty", "Rare Beauty", "MAC", "Estée Lauder", "YSL", "NARS", "Guerlain", "Charlotte Tilbury")
            val cosmeticColors = listOf("#8B0000", "#FFC0CB", "#D4AF37", "#2C2420", "#FDEEF4", "#E8F1FD", "#FEECEB", "#800020", "#C71585", "#DB7093")
            
            repeat(10) { i ->
                val micro = MicroCategory.entries.toTypedArray().random()
                val brand = cosmeticBrands[i % cosmeticBrands.size]
                val name = "$brand ${micro.displayName} Elite"
                
                cosmeticDao.insertCosmetic(
                    CosmeticItemEntity(
                        name = name,
                        brand = brand,
                        macroCategory = micro.macro,
                        microCategory = micro,
                        colorHex = cosmeticColors[i % cosmeticColors.size],
                        shadeName = "Signature ${i + 1}",
                        timestamp = System.currentTimeMillis() - (i * 3600000)
                    )
                )
            }

            // 2. Generate 10 High-Fidelity Clothing Items
            val clothingBrands = listOf("Atelier", "Saint Laurent", "Celine", "Brunello Cucinelli", "Loro Piana", "Hermès", "The Row", "Prada", "Gucci", "Loewe")
            val clothingColors = listOf("#222222", "#FFFFFF", "#F5F5DC", "#000080", "#355E3B", "#4A2C2A", "#708090", "#800000", "#FFD700", "#E1C16E")

            repeat(10) { i ->
                val category = ClothingCategory.entries.toTypedArray().random()
                val brand = clothingBrands[i % clothingBrands.size]
                val formality = if (i % 3 == 0) Formality.PROFESSIONAL else if (i % 5 == 0) Formality.FORMAL else Formality.CASUAL
                
                clothingDao.insertClothing(
                    ClothingItemEntity(
                        name = "$brand ${category.displayName} Piece",
                        brand = brand,
                        category = category,
                        formality = formality,
                        colorHex = clothingColors[i % clothingColors.size],
                        dominantHex = clothingColors[i % clothingColors.size],
                        material = "Premium Silk/Cashmere Blend",
                        price = (200..2000).random().toDouble(),
                        timestamp = System.currentTimeMillis() - (i * 7200000)
                    )
                )
            }
        }
    }
}
