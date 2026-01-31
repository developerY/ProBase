package com.zoewave.probase.core.data.mappers


import com.zoewave.probase.core.model.yelp.BusinessInfo
import com.zoewave.probase.core.model.yelp.Category
import com.zoewave.probase.core.model.yelp.Coordinates
import com.zoewave.probase.core.network.SearchYelpQuery


fun SearchYelpQuery.Business.toBusinessInfo(): BusinessInfo {
    return BusinessInfo(
        id = id ?: "No ID",
        name = name ?: "No Name",
        url = url ?: "No web address",
        rating = rating,
        photos = photos?.mapNotNull { it },
        price = price ?: "No Price",
        coordinates = Coordinates(
            coordinates?.latitude,
            coordinates?.longitude
        ),
        categories = categories?.mapNotNull { Category(it?.title) }
    )
}