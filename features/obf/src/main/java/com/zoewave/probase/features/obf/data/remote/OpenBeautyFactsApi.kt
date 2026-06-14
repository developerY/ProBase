package com.zoewave.probase.features.obf.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// 1. Data Transfer Objects (DTOs)
data class ObfResponse(
    val code: String,
    val status: Int, // 1 if found, 0 if not found
    val product: ObfProduct?
)

data class ObfWriteResponse(
    val status: Int,
    @SerializedName("status_verbose") val statusVerbose: String?
)

data class ObfProduct(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("categories_tags") val categoriesTags: List<String>?,
    @SerializedName("ingredients_text") val ingredientsText: String?,
    @SerializedName("ingredients_tags") val ingredientsTags: List<String>?,
    @SerializedName("image_front_url") val imageUrl: String?,
    @SerializedName("quantity") val volume: String?,
    @SerializedName("_keywords") val keywords: List<String>?,
    @SerializedName("ecoscore_grade") val ecoScore: String?,
    @SerializedName("allergens_tags") val allergensTags: List<String>?,
    @SerializedName("ingredients_analysis_tags") val analysisTags: List<String>?
)

// 2. Retrofit API Interface
interface OpenBeautyFactsApi {
    @GET("api/v1/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Response<ObfResponse>

    @FormUrlEncoded
    @POST("cgi/product_jqm2.pl")
    suspend fun uploadProduct(
        @Field("code") barcode: String,
        @Field("user_id") userId: String,
        @Field("password") password: String,
        @Field("product_name") productName: String?,
        @Field("brands") brands: String?,
        @Field("categories") categories: String?,
        @Field("ingredients_text") ingredients: String?,
        @Field("quantity") volume: String?,
        @Field("comment") comment: String = "Uploaded via KoColor App"
    ): Response<ObfWriteResponse>

    companion object {
        const val BASE_URL = "https://world.openbeautyfacts.org/"
    }
}
