package com.zoewave.probase.kocolor.features.colors.data.remote

import com.zoewave.probase.kocolor.features.colors.data.remote.model.ColorIdResponse
import com.zoewave.probase.kocolor.features.colors.data.remote.model.ColorSchemeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ColorApiService {

    @GET("id")
    suspend fun getColorId(
        @Query("hex") hex: String
    ): ColorIdResponse

    @GET("scheme")
    suspend fun getColorScheme(
        @Query("hex") hex: String,
        @Query("mode") mode: String,
        @Query("count") count: Int = 5
    ): ColorSchemeResponse

    companion object {
        const val BASE_URL = "https://www.thecolorapi.com/"
    }
}
