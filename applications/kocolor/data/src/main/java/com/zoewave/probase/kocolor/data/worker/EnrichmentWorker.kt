package com.zoewave.probase.kocolor.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.kocolor.db.dao.ProductDao
import com.zoewave.probase.kocolor.db.entity.EnrichmentStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withTimeout
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
            val modelName = aiSettings.aiModelFlow.firstOrNull() ?: "gemini-3.8-flash"
            
            val model = Firebase.ai(
                backend = GenerativeBackend.googleAI(),
                useLimitedUseAppCheckTokens = false
            ).generativeModel(
                modelName = modelName
            )
            
            val prompt = """
                You are a cosmetic chemistry assistant. Enrich this product data.
                Brand: ${product.brand}
                Name: ${product.productName}
                Ingredients: ${product.ingredients.joinToString()}
                
                Return JSON with keys: "skinConcerns" (list), "benefits" (list), "ingredientDescriptions" (map of name to short benefit).
                Return ONLY the raw JSON.
            """.trimIndent()

            // 1. DETERMINISTIC TIMEOUT BOUND (8 Seconds per user request)
            val response = withTimeout(8000) {
                model.generateContent(prompt)
            }

            val jsonText = response.text?.replace("```json", "")?.replace("```", "")?.trim()
            if (jsonText != null) {
                val enrichment = json.decodeFromString<EnrichmentResponse>(jsonText)

                val updatedProduct = product.copy(
                    skinConcerns = enrichment.skinConcerns,
                    benefits = enrichment.benefits,
                    enrichmentStatus = EnrichmentStatus.COMPLETED
                )
                productDao.updateProduct(updatedProduct)
                return ListenableWorker.Result.success()
            } else {
                productDao.updateProduct(product.copy(enrichmentStatus = EnrichmentStatus.FAILED))
                return ListenableWorker.Result.failure()
            }
        } catch (e: Exception) {
            when (e) {
                is SocketTimeoutException, is UnknownHostException -> {
                    // Retriable network error
                    productDao.updateProduct(product.copy(enrichmentStatus = EnrichmentStatus.PENDING))
                    return ListenableWorker.Result.retry()
                }
                else -> {
                    productDao.updateProduct(product.copy(enrichmentStatus = EnrichmentStatus.FAILED))
                    return ListenableWorker.Result.failure()
                }
            }
        }
    }
}
