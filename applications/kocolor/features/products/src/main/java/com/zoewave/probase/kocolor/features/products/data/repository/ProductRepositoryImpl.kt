package com.zoewave.probase.kocolor.features.products.data.repository

import com.zoewave.probase.kocolor.features.products.data.remote.OpenProductsFactsApi
import com.zoewave.probase.kocolor.features.products.data.remote.OpfProduct
import com.zoewave.probase.kocolor.features.products.domain.model.Product
import com.zoewave.probase.kocolor.features.products.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: OpenProductsFactsApi
) : ProductRepository {

    override suspend fun getProduct(barcode: String): Result<Product?> {
        return try {
            val response = api.getProductByBarcode(barcode)
            if (response.isSuccessful) {
                val opfResponse = response.body()
                if (opfResponse != null && opfResponse.status == 1) {
                    Result.success(opfResponse.product?.toDomain(opfResponse.code))
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun OpfProduct.toDomain(barcode: String): Product {
        return Product(
            id = barcode,
            name = productName ?: "Unknown Product",
            brand = brands ?: "Unknown Brand",
            imageUrl = imageUrl,
            fabricComposition = ingredientsText,
            materials = materialsTags?.map { it.replace("en:", "") } ?: emptyList(),
            origin = origins ?: manufacturingPlaces,
            ecoScore = ecoScoreGrade,
            sustainabilityLabels = labelsTags?.map { it.replace("en:", "") } ?: emptyList()
        )
    }
}
