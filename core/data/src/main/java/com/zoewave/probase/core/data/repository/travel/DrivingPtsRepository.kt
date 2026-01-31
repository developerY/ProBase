package com.zoewave.probase.core.data.repository.travel

interface DrivingPtsRepository {
    suspend fun getDrivingPts(org: String, des: String): String
}