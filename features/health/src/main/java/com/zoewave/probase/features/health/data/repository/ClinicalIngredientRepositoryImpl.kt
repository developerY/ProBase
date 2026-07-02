package com.zoewave.probase.features.health.data.repository

import com.zoewave.probase.features.health.data.remote.RxNavApiService
import javax.inject.Inject

class ClinicalIngredientRepositoryImpl @Inject constructor(
    private val apiService: RxNavApiService
) : ClinicalIngredientRepository {

    override suspend fun getStandardConceptId(ingredientName: String): Result<String?> = runCatching {
        val response = apiService.getRxCui(ingredientName)
        response.idGroup?.rxnormId?.firstOrNull()
    }

    override suspend fun getIngredientInteractions(rxcui: String): Result<List<String>> = runCatching {
        val response = apiService.getInteractions(rxcui)
        val interactions = mutableListOf<String>()

        response.interactionTypeGroup?.forEach { group ->
            group.interactionType?.forEach { type ->
                type.interactionPair?.forEach { pair ->
                    pair.description?.let { interactions.add(it) }
                }
            }
        }
        interactions.toList()
    }
}
