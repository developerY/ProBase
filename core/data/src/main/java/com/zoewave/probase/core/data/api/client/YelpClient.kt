package com.zoewave.probase.core.data.api.client

import com.apollographql.apollo3.ApolloClient
import com.zoewave.probase.core.data.api.interfaces.YelpAPI
import com.zoewave.probase.core.data.mappers.toBusinessInfo
import com.zoewave.probase.core.model.yelp.BusinessInfo
import com.zoewave.probase.core.network.SearchYelpQuery


import javax.inject.Inject

class YelpClient @Inject constructor(
    private val apolloClient: ApolloClient
) : YelpAPI {

    override suspend fun getBusinesses(
        latitude: Double,
        longitude: Double,
        radius: Double,
        sort_by: String,
        categories: String
    ): List<BusinessInfo?> {
        return apolloClient.query(
            SearchYelpQuery(
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                sort_by = sort_by,
                categories = categories
            )
        ).execute()
            .data
            ?.search
            ?.business
            ?.map { it?.toBusinessInfo() }
            ?: emptyList<BusinessInfo>()
    }
}