package com.zoewave.probase.core.data.api.interfaces

import com.zoewave.probase.core.model.yelp.BusinessInfo


interface YelpAPI {
    suspend fun getBusinesses(
        latitude: Double,
        longitude: Double,
        radius: Double,
        sort_by: String,
        categories: String
    ): List<BusinessInfo?>?
}