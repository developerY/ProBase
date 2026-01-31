package com.zoewave.probase.core.data.api.interfaces

interface MapsAPI {
    fun getMapDirections(org: String, des: String): String
}