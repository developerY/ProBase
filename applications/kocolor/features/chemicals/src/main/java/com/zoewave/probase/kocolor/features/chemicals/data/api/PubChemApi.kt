package com.zoewave.probase.kocolor.features.chemicals.data.api

import com.zoewave.probase.kocolor.features.chemicals.data.model.PubChemResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PubChemApi {
    @GET("compound/name/{name}/JSON")
    suspend fun getCompoundByName(@Path("name") name: String): PubChemResponse

    @GET("compound/cid/{cid}/JSON")
    suspend fun getCompoundByCid(@Path("cid") cid: Int): PubChemResponse
}
