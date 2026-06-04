package com.zoewave.probase.kocolor.features.products.domain.repository

import com.zoewave.probase.kocolor.features.products.domain.model.Product

interface ProductRepository {
    suspend fun getProduct(barcode: String): Result<Product?>
}
