package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScore

data class StyleResultUiState(
    val blueprint: StyleBlueprint? = null,
    val fashionistaScore: FashionistaScore? = null,
    val fashionistaCoverage: Float? = null,
    val calibrationVersion: String? = null,
    val selectedClothing: List<ClothingItem> = emptyList(),
    val selectedCosmetics: List<CosmeticItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
