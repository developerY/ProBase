package com.zoewave.probase.kocolor.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.ai.client.generativeai.GenerativeModel
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.kocolor.db.dao.ProductDao
import com.zoewave.probase.kocolor.db.entity.EnrichmentStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class EnrichmentResponse(
    val skinConcerns: List<String> = emptyList(),
    val benefits: List<String> = emptyList(),
    val ingredientDescriptions: Map<String, String> = emptyMap()
)

@HiltWorker
class EnrichmentWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val productDao: ProductDao,
    private val aiSettings: AiConfigurationSettings
) : CoroutineWorker(context, workerParams) {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): ListenableWorker.Result {
        val productId = inputData.getLong("product_id", -1L)
        if (productId == -1L) return ListenableWorker.Result.failure()

        val product = productDao.getProductById(productId) ?: return ListenableWorker.Result.failure()
        
        if (product.enrichmentStatus == EnrichmentStatus.COMPLETED) return ListenableWorker.Result.success()

        // Stage 5 transition
        productDao.updateProduct(product.copy(enrichmentStatus = EnrichmentStatus.ENRICHING))

        try {
            val apiKey = aiSettings.getGeminiApiKey() ?: return ListenableWorker.Result.retry()
            val modelName = aiSettings.aiModelFlow.firstOrNull() ?: "gemini-1.5-flash"
            
            val model = GenerativeModel(modelName = modelName, apiKey = apiKey)
            
            val prompt = """
                You are a cosmetic chemistry assistant. Enrich this product data.
                Brand: ${product.brand}
                Name: ${product.productName}
                Ingredients: ${product.ingredients.joinToString()}
                
                Return JSON with keys: "skinConcerns" (list), "benefits" (list), "ingredientDescriptions" (map of name to short benefit).
                Return ONLY the raw JSON.
            """.trimIndent()

            val response = model.generateContent(prompt)
            val jsonText = response.text ?: return ListenableWorker.Result.failure()
            
            // Safe JSON Extraction
            val jsonRegex = Regex("""\{[\s\S]*\}""")
            val match = jsonRegex.find(jsonText) ?: return ListenableWorker.Result.failure()
            val enrichedData = json.decodeFromString<EnrichmentResponse>(match.value)
            
            // Final Database Entity Update
            productDao.updateProduct(product.copy(
                skinConcerns = enrichedData.skinConcerns,
                benefits = enrichedData.benefits,
                ingredientDescriptions = enrichedData.ingredientDescriptions,
                enrichmentStatus = EnrichmentStatus.COMPLETED
            ))

            return ListenableWorker.Result.success()
        } catch (e: Exception) {
            android.util.Log.e("EnrichmentWorker", "Error enriching product: ${e.message}")
            return ListenableWorker.Result.retry()
        }
    }
}
