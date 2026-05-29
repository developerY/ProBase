package com.zoewave.probase.features.obf.data.repository

import com.zoewave.probase.features.obf.data.mapper.ObfTaxonomyMapper
import com.zoewave.probase.features.obf.data.remote.OpenBeautyFactsApi
import com.zoewave.probase.kocolor.model.CosmeticItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObfRepository @Inject constructor(
    private val api: OpenBeautyFactsApi
) {

    /**
     * Fetches a product by Barcode from Open Beauty Facts, sanitizes the data,
     * and returns a partial CosmeticItem ready for user confirmation.
     */
    suspend fun fetchProductByBarcode(barcode: String): Result<CosmeticItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProductByBarcode(barcode)
            
            if (response.isSuccessful && response.body()?.status == 1) {
                val obfProduct = response.body()?.product ?: return@withContext Result.failure(Exception("Product data is missing."))
                
                // Map OBF taxonomy to KoColor strict enums
                val resolvedMicroCategory = ObfTaxonomyMapper.extractMicroCategory(obfProduct.categoriesTags)
                val resolvedMacroCategory = resolvedMicroCategory.macro // Inherited automatically
                
                // Join ingredients for the notes or instructions if needed, 
                // but the blueprint uses it as a list for the Conflict Engine.
                // CosmeticItem currently doesn't have an ingredients list field, only notes.
                // We'll put it in notes for now, or instructions.
                val ingredientList = ObfTaxonomyMapper.parseIngredients(obfProduct.ingredientsText)
                val ingredientNotes = if (ingredientList.isNotEmpty()) {
                    "Ingredients: ${ingredientList.joinToString(", ")}"
                } else null

                val draftItem = CosmeticItem(
                    batchCode = barcode, // Use barcode as the batch code / SKU
                    brand = obfProduct.brands ?: "Unknown Brand",
                    name = obfProduct.productName ?: "Unknown Product",
                    macroCategory = resolvedMacroCategory,
                    microCategory = resolvedMicroCategory,
                    imageUrl = obfProduct.imageUrl,
                    notes = ingredientNotes,
                    volume = obfProduct.volume
                )
                
                Result.success(draftItem)
            } else {
                Result.failure(Exception("Product not found in Open Beauty Facts database."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
