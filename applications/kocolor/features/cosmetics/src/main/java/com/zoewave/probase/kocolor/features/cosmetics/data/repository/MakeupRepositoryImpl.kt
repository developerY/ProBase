package com.zoewave.probase.kocolor.features.cosmetics.data.repository

import com.zoewave.probase.kocolor.features.cosmetics.data.remote.MakeupApiService
import com.zoewave.probase.kocolor.features.cosmetics.data.remote.model.MakeupProductDto
import com.zoewave.probase.kocolor.features.cosmetics.domain.repository.MakeupRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MakeupRepositoryImpl @Inject constructor(
    private val apiService: MakeupApiService
) : MakeupRepository {

    override suspend fun searchProducts(
        brand: String?,
        productType: String?,
        category: String?,
        tags: List<String>?
    ): Result<List<MakeupProductDto>> = runCatching {
        val tagString = tags?.joinToString(",")
        apiService.searchProducts(
            brand = brand,
            productType = productType,
            category = category,
            tags = tagString
        )
    }
}
