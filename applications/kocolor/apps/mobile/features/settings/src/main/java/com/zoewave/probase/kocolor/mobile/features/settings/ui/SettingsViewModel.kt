package com.zoewave.probase.kocolor.mobile.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.KoColorSettings
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.model.*
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
    val currentTheme: String = "SYSTEM",
    val currentPalette: String = "CLASSIC"
)

sealed class SettingsEvent {
    data class OnAiExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnAboutExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnThemeExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnPaletteExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnHealthExpandedToggled(val expanded: Boolean) : SettingsEvent()
    data class OnThemeSelected(val theme: String) : SettingsEvent()
    data class OnPaletteSelected(val palette: String) : SettingsEvent()
    data object OnGenerateSampleCosmetics : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val koSettings: KoColorSettings,
    private val cosmeticDao: CosmeticDao
) : ViewModel() {

    private val _expandState = MutableStateFlow(
        listOf(false, false, false, false, false) // AI, About, Theme, Palette, Health
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        _expandState,
        koSettings.appThemeFlow,
        koSettings.colorPaletteFlow
    ) { expands, theme, palette ->
        SettingsUiState(
            isAiExpanded = expands[0],
            isAboutExpanded = expands[1],
            isThemeExpanded = expands[2],
            isPaletteExpanded = expands[3],
            isHealthExpanded = expands[4],
            currentTheme = theme,
            currentPalette = palette
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
            SettingsEvent.OnGenerateSampleCosmetics -> {
                generateSampleItems()
            }
        }
    }

    private fun generateSampleItems() {
        viewModelScope.launch {
            val brands = listOf("L'Oreal", "MAC", "Maybelline", "Chanel", "Dior", "Fenty Beauty", "Rare Beauty")
            val productSuffixes = listOf("Glow", "Matte", "Gloss", "Cream", "Stick", "Powder", "Ink")
            val colors = listOf("#FF0000", "#FFC0CB", "#800020", "#C71585", "#DB7093", "#FFA07A", "#FF7F50")
            
            repeat(50) { i ->
                val micro = MicroCategory.entries.toTypedArray().random()
                val brand = brands.random()
                val name = "${brand} ${productSuffixes.random()} ${i + 1}"
                
                cosmeticDao.insertCosmetic(
                    CosmeticItemEntity(
                        name = name,
                        brand = brand,
                        macroCategory = micro.macro,
                        microCategory = micro,
                        colorHex = colors.random(),
                        shadeName = "Shade ${i + 1}",
                        timestamp = System.currentTimeMillis() - (i * 1000 * 60 * 60) // Different times
                    )
                )
            }
        }
    }
}
