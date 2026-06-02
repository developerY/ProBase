package com.zoewave.probase.features.fda.data.repository

import com.zoewave.probase.features.fda.data.remote.FdaApi
import com.zoewave.probase.features.fda.data.remote.FdaEnforcementResult
import com.zoewave.probase.features.fda.data.remote.FdaEventResult
import com.zoewave.probase.features.fda.data.remote.FdaLabelResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FdaRepository @Inject constructor(
    private val api: FdaApi
) {

    suspend fun getRecalls(brand: String, productName: String): FdaEnforcementResult? = withContext(Dispatchers.IO) {
        val brandSafe = brand.replace(" ", "+")
        val nameSafe = productName.split(" ").firstOrNull() ?: ""
        val query = "product_description:\"$brandSafe\"+AND+product_description:\"$nameSafe\"+AND+status:\"Ongoing\""
        
        // Try food (cosmetics) first
        val foodResponse = try { api.searchFoodEnforcements(query) } catch (e: Exception) { null }
        if (foodResponse?.isSuccessful == true) {
            val result = foodResponse.body()?.results?.firstOrNull()
            if (result != null) return@withContext result
        }

        // Then drug (OTCs)
        val drugResponse = try { api.searchDrugEnforcements(query) } catch (e: Exception) { null }
        if (drugResponse?.isSuccessful == true) {
            val result = drugResponse.body()?.results?.firstOrNull()
            if (result != null) return@withContext result
        }

        null
    }

    suspend fun getAdverseEventsCount(brand: String, productName: String): Int = withContext(Dispatchers.IO) {
        val brandSafe = brand.replace(" ", "+")
        val nameSafe = productName.split(" ").firstOrNull() ?: ""
        val query = "products.product_name:\"$brandSafe\"+AND+products.product_name:\"$nameSafe\""
        val response = try { api.searchFoodEvents(query, limit = 1) } catch (e: Exception) { null }
        response?.body()?.meta?.results?.total ?: 0
    }

    suspend fun getTopReactions(brand: String, productName: String): List<String> = withContext(Dispatchers.IO) {
        val brandSafe = brand.replace(" ", "+")
        val nameSafe = productName.split(" ").firstOrNull() ?: ""
        val query = "products.product_name:\"$brandSafe\"+AND+products.product_name:\"$nameSafe\""
        val response = try { api.searchFoodEvents(query, limit = 10) } catch (e: Exception) { null }
        response?.body()?.results?.flatMap { it.reactions }?.distinct() ?: emptyList()
    }

    suspend fun getDrugLabel(barcode: String): FdaLabelResult? = withContext(Dispatchers.IO) {
        val query = "openfda.upc:\"$barcode\""
        val response = try { api.searchDrugLabels(query) } catch (e: Exception) { null }
        if (response?.isSuccessful == true) return@withContext response.body()?.results?.firstOrNull()
        null
    }
}
