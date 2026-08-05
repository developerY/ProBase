package com.zoewave.probase.kocolor.features.starterpack.data.remote

import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.SearchIndexEntry
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.SignedPayloadEnvelope
import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Path

interface KocolorApiService {
    @GET("manifest.json")
    suspend fun getManifest(): SignedPayloadEnvelope<JsonElement>

    @GET("{endpoint}")
    suspend fun getPack(@Path("endpoint") endpoint: String): SignedPayloadEnvelope<JsonElement>

    @GET("search_index.json")
    suspend fun getSearchIndex(): List<SearchIndexEntry>

    @GET("packs/{packId}.json")
    suspend fun getPackItems(@Path("packId") packId: String): SignedPayloadEnvelope<JsonElement>

    companion object {
        const val BASE_URL = "https://cdn.kocolor.com/inventory/"
    }
}
