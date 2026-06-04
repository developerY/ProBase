package com.zoewave.probase.kocolor.features.colors.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class ColorResponse(
    @SerializedName("hex") val hex: HexDto,
    @SerializedName("name") val name: NameDto,
    @SerializedName("contrast") val contrast: ContrastDto
)

data class HexDto(
    @SerializedName("value") val value: String,
    @SerializedName("clean") val clean: String
)

data class NameDto(
    @SerializedName("value") val value: String,
    @SerializedName("closest_named_hex") val closestNamedHex: String,
    @SerializedName("exact_match_name") val exactMatchName: Boolean
)

data class ContrastDto(
    @SerializedName("value") val value: String
)

data class SchemeResponse(
    @SerializedName("mode") val mode: String,
    @SerializedName("count") val count: Int,
    @SerializedName("colors") val colors: List<ColorResponse>
)

interface ColorApi {
    @GET("id")
    suspend fun getColorById(@Query("hex") hex: String): Response<ColorResponse>

    @GET("scheme")
    suspend fun getColorScheme(
        @Query("hex") hex: String,
        @Query("mode") mode: String = "complement",
        @Query("count") count: Int = 5
    ): Response<SchemeResponse>

    companion object {
        const val BASE_URL = "https://www.thecolorapi.com/"
    }
}
