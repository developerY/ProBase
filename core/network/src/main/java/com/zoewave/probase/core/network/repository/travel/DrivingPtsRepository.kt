package com.zoewave.probase.core.network.repository.travel

interface DrivingPtsRepository {
    suspend fun getDrivingPts(org: String, des: String): String
}