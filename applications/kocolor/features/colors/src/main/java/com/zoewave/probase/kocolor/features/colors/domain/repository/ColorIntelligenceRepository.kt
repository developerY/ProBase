package com.zoewave.probase.kocolor.features.colors.domain.repository

import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.kocolor.features.colors.domain.model.ColorSignature
import com.zoewave.probase.kocolor.features.colors.domain.model.HarmonyMode
import com.zoewave.probase.kocolor.features.colors.domain.model.StylistEdit
import kotlinx.coroutines.flow.Flow

interface ColorIntelligenceRepository {
    fun getAllInventoryColors(): Flow<List<ColorSignature>>
    fun getColorsByHarmony(baseHex: String, mode: HarmonyMode): Flow<List<ColorSignature>>
    fun getPaletteGaps(userSeason: SeasonalType): Flow<List<String>>
    fun getStylistEdit(userSeason: SeasonalType): Flow<StylistEdit>
}
