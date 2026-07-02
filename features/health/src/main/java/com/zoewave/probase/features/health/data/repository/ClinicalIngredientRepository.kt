package com.zoewave.probase.features.health.data.repository

interface ClinicalIngredientRepository {
    /**
     * Translates an ingredient name (e.g., "Salicylic Acid") into its standard RxCUI.
     */
    suspend fun getStandardConceptId(ingredientName: String): Result<String?>

    /**
     * Fetches clinical drug interactions for a specific RxCUI.
     */
    suspend fun getIngredientInteractions(rxcui: String): Result<List<String>>
}
