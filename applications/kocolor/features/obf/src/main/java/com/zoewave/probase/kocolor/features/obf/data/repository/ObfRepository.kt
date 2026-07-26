package com.zoewave.probase.kocolor.features.obf.data.repository

import com.zoewave.probase.kocolor.features.obf.data.mapper.ObfTaxonomyMapper
import com.zoewave.probase.kocolor.features.obf.data.remote.OpenBeautyFactsApi
import com.zoewave.probase.core.model.ritual.CosmeticItem
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
                
                // 1. Map OBF taxonomy to KoColor strict enums
                val resolvedMicroCategory = ObfTaxonomyMapper.extractMicroCategory(obfProduct.categoriesTags)
                val resolvedMacroCategory = resolvedMicroCategory.macro // Inherited automatically

                // 2. Fragment technical details
                var ingredientList = ObfTaxonomyMapper.parseIngredients(obfProduct.ingredientsText)
                
                // Fallback to ingredients_tags if text is missing
                if (ingredientList.isEmpty() && !obfProduct.ingredientsTags.isNullOrEmpty()) {
                    ingredientList = obfProduct.ingredientsTags.map { 
                        it.replace("en:", "").replace("-", " ").replaceFirstChar { c -> c.uppercase() } 
                    }
                }
                
                // 3. Heuristics for Professional Facets
                val resolvedFinish = ObfTaxonomyMapper.extractFinish(obfProduct.keywords)
                val resolvedFormulation = ObfTaxonomyMapper.extractFormulation(obfProduct.keywords)
                val resolvedChemistry = ObfTaxonomyMapper.extractChemistryBase(ingredientList)

                // 4. Ingredient Highlights & Allergens
                val heroIngredient = ingredientList.firstOrNull()?.replaceFirstChar { it.uppercase() }
                val hasFragrance = ingredientList.any { it.contains("parfum") || it.contains("fragrance") }
                val allergens = ObfTaxonomyMapper.extractAllergens(obfProduct.allergensTags)

                val draftItem = CosmeticItem(
                    batchCode = barcode,
                    brand = obfProduct.brands?.split(",")?.firstOrNull()?.trim() ?: "Unknown Brand",
                    name = obfProduct.productName ?: "Unknown Product",
                    macroCategory = resolvedMacroCategory,
                    microCategory = resolvedMicroCategory,
                    formulation = resolvedFormulation,
                    finish = resolvedFinish,
                    chemistryBase = resolvedChemistry,
                    heroIngredient = heroIngredient,
                    containsFragrance = hasFragrance,
                    ingredients = ingredientList,
                    allergens = allergens,
                    ecoScore = obfProduct.ecoScore?.uppercase(),
                    isVegan = ObfTaxonomyMapper.isVegan(obfProduct.analysisTags),
                    isCrueltyFree = ObfTaxonomyMapper.isCrueltyFree(obfProduct.keywords),
                    imageUrl = obfProduct.imageUrl,
                    volume = obfProduct.volume,
                    colorHex = "#FFFFFF"
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
