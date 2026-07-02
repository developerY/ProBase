package com.zoewave.probase.kocolor.features.makeupapi.data.remote

import com.zoewave.probase.kocolor.features.makeupapi.data.remote.model.MakeupProductDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MakeupApiService {

    @GET("products.json")
    suspend fun searchProducts(
        @Query("brand") brand: String? = null,
        @Query("product_type") productType: String? = null,
        @Query("product_category") category: String? = null,
        @Query("product_tags") tags: String? = null
    ): List<MakeupProductDto>

    companion object {
        const val BASE_URL = "http://makeup-api.herokuapp.com/api/v1/"
    }
}
