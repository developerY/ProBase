package com.zoewave.probase.kocolor.data.remote

import com.zoewave.probase.kocolor.data.remote.model.StarterPackResponse
import retrofit2.http.GET

interface KocolorApiService {
    @GET("starter-pack.json")
    suspend fun getStarterPack(): StarterPackResponse

    companion object {
        const val BASE_URL = "https://cdn.kocolor.com/inventory/"
    }
}
