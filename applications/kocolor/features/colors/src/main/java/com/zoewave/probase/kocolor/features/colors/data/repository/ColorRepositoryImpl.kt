package com.zoewave.probase.kocolor.features.colors.data.repository

import com.zoewave.probase.kocolor.features.colors.data.remote.ColorApi
import com.zoewave.probase.kocolor.features.colors.domain.model.ColorInfo
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorRepository
import javax.inject.Inject

class ColorRepositoryImpl @Inject constructor(
    private val colorApi: ColorApi
) : ColorRepository {

    override suspend fun getColorDetails(hex: String): Result<ColorInfo> {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorResponse = colorApi.getColorById(cleanHex)
            
            if (colorResponse.isSuccessful && colorResponse.body() != null) {
                val colorData = colorResponse.body()!!
                
                // Fetch complementary palette
                val schemeResponse = colorApi.getColorScheme(cleanHex, "complement")
                val paletteData = schemeResponse.body()?.colors?.map { it.hex.value } ?: emptyList()

                val info = ColorInfo(
                    hex = colorData.hex.value,
                    name = colorData.name.value,
                    // Standardized color name and a mock Pantone match for implementation purposes
                    pantoneMatch = "PANTONE ${colorData.name.value.uppercase().replace(" ", "-")}",
                    complementaryPalette = paletteData
                )
                Result.success(info)
            } else {
                Result.failure(Exception("Failed to fetch color data: ${colorResponse.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
