package com.zoewave.probase.features.health.data.remote

import com.zoewave.probase.features.health.data.remote.model.InteractionResponse
import com.zoewave.probase.features.health.data.remote.model.RxCuiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RxNavApiService {

    @GET("rxcui.json")
    suspend fun getRxCui(
        @Query("name") ingredientName: String
    ): RxCuiResponse

    @GET("interaction/interaction.json")
    suspend fun getInteractions(
        @Query("rxcui") rxcui: String
    ): InteractionResponse

    companion object {
        const val BASE_URL = "https://rxnav.nlm.nih.gov/REST/"
    }
}
