package com.zoewave.probase.features.fda.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FdaApi {

    @GET("food/enforcement.json")
    suspend fun searchFoodEnforcements(
        @Query("search") search: String,
        @Query("limit") limit: Int = 1
    ): Response<FdaResponse<FdaEnforcementResult>>

    @GET("drug/enforcement.json")
    suspend fun searchDrugEnforcements(
        @Query("search") search: String,
        @Query("limit") limit: Int = 1
    ): Response<FdaResponse<FdaEnforcementResult>>

    @GET("food/event.json")
    suspend fun searchFoodEvents(
        @Query("search") search: String,
        @Query("limit") limit: Int = 10
    ): Response<FdaResponse<FdaEventResult>>

    @GET("drug/label.json")
    suspend fun searchDrugLabels(
        @Query("search") search: String,
        @Query("limit") limit: Int = 1
    ): Response<FdaResponse<FdaLabelResult>>

    companion object {
        const val BASE_URL = "https://api.fda.gov/"
    }
}
