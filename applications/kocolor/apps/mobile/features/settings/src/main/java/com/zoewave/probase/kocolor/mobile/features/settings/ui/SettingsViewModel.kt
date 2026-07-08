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
import com.zoewave.probase.core.util.color.ColorQuantizer
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
            // Realistic Color Palettes
            val lipColors = listOf("#8B0000", "#FFC0CB", "#E9967A", "#D8BFD8", "#FA8072", "#C71585", "#DB7093", "#FF69B4", "#B03060", "#DC143C")
            val cheekColors = listOf("#FFB6C1", "#FFDAB9", "#CD7F32", "#BC8F8F", "#FF7F50", "#DB7093", "#E9967A", "#F08080")
            val eyeColors = listOf("#3E2723", "#F5DEB3", "#B87333", "#808080", "#2F4F4F", "#000080", "#556B2F", "#D2691E", "#A0522D")
            val complexColors = listOf("#F5F5DC", "#FFE4C4", "#DEB887", "#F3E5AB", "#ECE2C6", "#FDF5E6", "#FAEBD7")
            val neutralClothing = listOf("#000000", "#FFFFFF", "#000080", "#808080", "#B38B6D", "#F5F5DC", "#2F4F4F", "#355E3B")
            val classicClothing = listOf("#800000", "#50C878", "#FFD700", "#E1C16E", "#708090", "#4A2C2A", "#1E3A8A", "#B91C1C")

            val cosmeticBrands = listOf("Chanel", "Dior", "Fenty Beauty", "Rare Beauty", "MAC", "Estée Lauder", "YSL", "NARS", "Guerlain", "Charlotte Tilbury", "Pat McGrath", "Hourglass")
            
            // 1. Generate 50 High-Fidelity Cosmetics
            repeat(50) { i ->
                val micro = MicroCategory.entries.toTypedArray().filter { it.macro in listOf(MacroCategory.LIPS, MacroCategory.EYES, MacroCategory.DIMENSION, MacroCategory.COMPLEXION) }.random()
                val brand = cosmeticBrands.random()
                val name = "$brand ${micro.displayName} Pro"
                
                val colors = when(micro.macro) {
                    MacroCategory.LIPS -> lipColors
                    MacroCategory.EYES -> eyeColors
                    MacroCategory.DIMENSION -> cheekColors
                    MacroCategory.COMPLEXION -> complexColors
                    else -> complexColors
                }
                
                val hex = colors.random()
                cosmeticDao.insertCosmetic(
                    CosmeticItemEntity(
                        name = name,
                        brand = brand,
                        macroCategory = micro.macro,
                        microCategory = micro,
                        colorHex = hex,
                        colorFamily = ColorQuantizer.snapToFamily(hex),
                        shadeName = "Artist Edition ${i + 1}",
                        notes = if (i % 5 == 0) "Long-wear high SPF formula" else "Professional pigment",
                        price = (25..85).random().toDouble(),
                        timestamp = System.currentTimeMillis() - (i * 3600000)
                    )
                )
            }

            // 2. Generate 50 High-Fidelity Clothing Items
            val clothingBrands = listOf("Atelier", "Saint Laurent", "Celine", "Brunello Cucinelli", "Loro Piana", "Hermès", "The Row", "Prada", "Gucci", "Loewe", "Tom Ford", "Zegna")
            
            repeat(50) { i ->
                val category = ClothingCategory.entries.toTypedArray().filter { it != ClothingCategory.OTHER }.random()
                val brand = clothingBrands.random()
                val formality = when {
                    i % 4 == 0 -> Formality.PROFESSIONAL
                    i % 6 == 0 -> Formality.FORMAL
                    i % 10 == 0 -> Formality.GALA
                    else -> Formality.CASUAL
                }
                
                val colors = if (i % 2 == 0) neutralClothing else classicClothing
                val hex = colors.random()
                
                clothingDao.insertClothing(
                    ClothingItemEntity(
                        name = "$brand ${category.displayName} Select",
                        brand = brand,
                        category = category,
                        formality = formality,
                        colorHex = hex,
                        colorFamily = ColorQuantizer.snapToFamily(hex),
                        dominantHex = hex,
                        material = if (i % 3 == 0) "Premium Silk/Cashmere Blend" else "High-Thread Performance Cotton",
                        price = (150..3500).random().toDouble(),
                        notes = "Archived from seasonal lookbook.",
                        timestamp = System.currentTimeMillis() - (i * 7200000)
                    )
                )
            }
        }
    }
}
