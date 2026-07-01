package com.zoewave.probase.kocolor.features.colors.domain.repository

interface ColorRepository {
    suspend fun getColorName(hex: String): Result<String>
    suspend fun getColorScheme(hex: String, mode: String): Result<List<String>>
}
