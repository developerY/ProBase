package com.zoewave.probase.features.obf.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

// 1. Data Transfer Objects (DTOs)
data class ObfResponse(
    val code: String,
    val status: Int, // 1 if found, 0 if not found
    val product: ObfProduct?
)

data class ObfProduct(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("categories_tags") val categoriesTags: List<String>?,
    @SerializedName("ingredients_text") val ingredientsText: String?,
    @SerializedName("image_front_url") val imageUrl: String?,
    @SerializedName("quantity") val volume: String?
)

// 2. Retrofit API Interface
interface OpenBeautyFactsApi {
    @GET("api/v1/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Response<ObfResponse>

    companion object {
        const val BASE_URL = "https://world.openbeautyfacts.org/"
    }
}
