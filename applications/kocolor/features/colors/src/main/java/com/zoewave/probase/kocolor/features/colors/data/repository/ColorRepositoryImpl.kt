package com.zoewave.probase.kocolor.features.colors.data.repository

import com.zoewave.probase.kocolor.features.colors.data.remote.ColorApiService
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColorRepositoryImpl @Inject constructor(
    private val apiService: ColorApiService
) : ColorRepository {

    override suspend fun getColorName(hex: String): Result<String> = runCatching {
        val cleanHex = hex.removePrefix("#")
        val response = apiService.getColorId(cleanHex)
        response.name.value
    }

    override suspend fun getColorScheme(hex: String, mode: String): Result<List<String>> = runCatching {
        val cleanHex = hex.removePrefix("#")
        val response = apiService.getColorScheme(cleanHex, mode)
        response.colors.map { it.hex.value }
    }
}
