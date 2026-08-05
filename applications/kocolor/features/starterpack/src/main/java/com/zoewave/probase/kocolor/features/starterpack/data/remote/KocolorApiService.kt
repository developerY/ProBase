package com.zoewave.probase.kocolor.features.starterpack.data.remote

import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.SignedPayloadEnvelope
import retrofit2.http.GET
import retrofit2.http.Path

interface KocolorApiService {
    @GET("manifest.json")
    suspend fun getManifest(): SignedPayloadEnvelope

    @GET("{endpoint}")
    suspend fun getPack(@Path("endpoint") endpoint: String): SignedPayloadEnvelope

    companion object {
        const val BASE_URL = "https://cdn.kocolor.com/inventory/"
    }
}
