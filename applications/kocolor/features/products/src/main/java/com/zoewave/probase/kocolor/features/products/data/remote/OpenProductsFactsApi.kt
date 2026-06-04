package com.zoewave.probase.kocolor.features.products.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

data class OpfResponse(
    val code: String,
    val status: Int,
    @SerializedName("status_verbose") val statusVerbose: String?,
    val product: OpfProduct?
)

data class OpfProduct(
    @SerializedName("product_name") val productName: String?,
    val brands: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("ingredients_text") val ingredientsText: String?,
    @SerializedName("materials_tags") val materialsTags: List<String>?,
    val origins: String?,
    @SerializedName("manufacturing_places") val manufacturingPlaces: String?,
    @SerializedName("ecoscore_grade") val ecoScoreGrade: String?,
    @SerializedName("labels_tags") val labelsTags: List<String>?
)

interface OpenProductsFactsApi {
    @GET("api/v2/product/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Response<OpfResponse>

    companion object {
        const val BASE_URL = "https://world.openproductsfacts.org/"
    }
}
