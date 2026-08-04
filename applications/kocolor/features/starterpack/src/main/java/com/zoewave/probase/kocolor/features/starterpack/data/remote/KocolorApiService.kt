package com.zoewave.probase.kocolor.features.starterpack.data.remote

import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackManifest
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.StarterPackResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface KocolorApiService {
    @GET("manifest.json")
    suspend fun getManifest(): PackManifest

    @GET("{endpoint}")
    suspend fun getPack(@Path("endpoint") endpoint: String): StarterPackResponse

    companion object {
        const val BASE_URL = "https://cdn.kocolor.com/inventory/"
    }
}
