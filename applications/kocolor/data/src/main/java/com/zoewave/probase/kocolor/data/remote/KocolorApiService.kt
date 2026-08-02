package com.zoewave.probase.kocolor.data.remote

import com.zoewave.probase.kocolor.data.remote.model.StarterPackResponse
import retrofit2.http.GET

interface KocolorApiService {
    @GET("api/v1/starter-pack")
    suspend fun getStarterPack(): StarterPackResponse

    companion object {
        const val BASE_URL = "http://10.0.2.2:3000/" // Android Emulator localhost
    }
}
