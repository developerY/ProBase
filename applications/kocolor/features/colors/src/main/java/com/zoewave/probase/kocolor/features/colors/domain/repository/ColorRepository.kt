package com.zoewave.probase.kocolor.features.colors.domain.repository

import com.zoewave.probase.kocolor.features.colors.domain.model.ColorInfo

interface ColorRepository {
    suspend fun getColorDetails(hex: String): Result<ColorInfo>
}
