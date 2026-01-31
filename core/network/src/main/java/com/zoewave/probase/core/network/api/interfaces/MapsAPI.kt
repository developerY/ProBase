package com.zoewave.probase.core.network.api.interfaces

interface MapsAPI {
    fun getMapDirections(org: String, des: String): String
}