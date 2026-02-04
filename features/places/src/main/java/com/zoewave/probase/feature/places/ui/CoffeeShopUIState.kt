package com.zoewave.probase.feature.places.ui

import com.zoewave.probase.core.model.yelp.BusinessInfo


sealed class CoffeeShopUIState {
    object Loading : CoffeeShopUIState()
    data class Error(val message: String) : CoffeeShopUIState()

    data class Success(
        val coffeeShops: List<BusinessInfo> = emptyList(),
    ) : CoffeeShopUIState()

}
