package com.zoewave.probase.kocolor.features.cosmetics.domain.repository

import com.zoewave.probase.kocolor.features.cosmetics.data.remote.model.MakeupProductDto

interface MakeupRepository {
    suspend fun searchProducts(
        brand: String? = null,
        productType: String? = null,
        category: String? = null,
        tags: List<String>? = null
    ): Result<List<MakeupProductDto>>
}
