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
        android.util.Log.d("ObfRepo", "fetchProductByBarcode: Querying API for $barcode")
        try {
            val response = api.getProductByBarcode(barcode)
            
            if (response.isSuccessful && response.body()?.status == 1) {
                val obfProduct = response.body()?.product ?: return@withContext Result.failure(Exception("Product data is missing."))
                android.util.Log.d("ObfRepo", "fetchProductByBarcode: SUCCESS. Found ${obfProduct.productName} by ${obfProduct.brands}")
                
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
                android.util.Log.w("ObfRepo", "fetchProductByBarcode: API returned success=false or status != 1 for $barcode. Status: ${response.body()?.status}")
                Result.failure(Exception("Product not found in Open Beauty Facts database."))
            }
        } catch (e: Exception) {
            android.util.Log.e("ObfRepo", "fetchProductByBarcode: EXCEPTION for $barcode", e)
            Result.failure(e)
        }
    }
}
