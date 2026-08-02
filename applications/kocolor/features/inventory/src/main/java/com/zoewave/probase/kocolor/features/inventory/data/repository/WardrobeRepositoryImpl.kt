package com.zoewave.probase.kocolor.features.inventory.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.mobile.features.color.domain.engine.WardrobeColorEngine
import com.zoewave.probase.core.util.color.ColorQuantizer
import com.zoewave.probase.core.model.ritual.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WardrobeRepositoryImpl"

@Singleton
class WardrobeRepositoryImpl @Inject constructor(
    private val clothingDao: ClothingDao,
    private val colorEngine: WardrobeColorEngine,
    private val apiService: KocolorApiService,
    @ApplicationContext private val context: Context
) : WardrobeRepository {

    override fun getAllClothing(): Flow<List<ClothingItem>> {
        return clothingDao.getAllClothing()
            .map { entities -> entities.map { it.toModel() } }
            .catch { e ->
                Log.e(TAG, "Error fetching all clothing items", e)
                emit(emptyList())
            }
    }

    override fun getShortlistByIntent(intent: String): Flow<List<ClothingItem>> {
        val minFormality = mapIntentToFormality(intent)
        return clothingDao.getClothingByMinFormality(minFormality)
            .map { entities -> entities.map { it.toModel() } }
            .catch { e ->
                Log.e(TAG, "Error fetching shortlist for intent: $intent", e)
                emit(emptyList())
            }
    }

    private fun mapIntentToFormality(intent: String): Formality {
        val lower = intent.lowercase()
        return when {
            lower.contains("negotiation") || lower.contains("boardroom") || lower.contains("professional") -> Formality.PROFESSIONAL
            lower.contains("gala") || lower.contains("wedding") || lower.contains("formal") -> Formality.FORMAL
            lower.contains("interview") || lower.contains("presentation") || lower.contains("date") -> Formality.SMART_CASUAL
            lower.contains("chill") || lower.contains("home") || lower.contains("relax") -> Formality.LOUNGE
            else -> Formality.CASUAL
        }
    }

    override fun getClothingById(id: Long): Flow<ClothingItem?> {
        return clothingDao.getClothingById(id)
            .map { it?.toModel() }
            .catch { e ->
                Log.e(TAG, "Error fetching clothing item by id: $id", e)
                emit(null)
            }
    }

    override suspend fun saveClothingItem(item: ClothingItem) {
        withContext(Dispatchers.IO) {
            try {
                val existingItem = if (item.id != 0L) clothingDao.getClothingById(item.id).firstOrNull()?.toModel() else null
                
                val needsAnalysis = item.imageUrl != null && (
                    item.dominantHex == null || 
                    item.imageUrl != existingItem?.imageUrl
                )

                val analyzedItem = if (needsAnalysis) {
                    analyzeGarment(item)
                } else item

                // Snap to perceptual color family
                val bucketedItem = analyzedItem.copy(
                    colorFamily = ColorQuantizer.snapToFamily(analyzedItem.colorHex)
                )

                clothingDao.insertClothing(bucketedItem.toEntity())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save clothing item: ${item.name}", e)
            }
        }
    }

    override suspend fun wearClothingItem(id: Long) {
        withContext(Dispatchers.IO) {
            try {
                clothingDao.getClothingById(id).firstOrNull()?.let { entity ->
                    val model = entity.toModel()
                    clothingDao.insertClothing(model.copy(usageCount = model.usageCount + 1).toEntity())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log wear for item: $id", e)
            }
        }
    }

    override suspend fun deleteClothing(id: Long) {
        withContext(Dispatchers.IO) {
            try {
                clothingDao.deleteClothing(id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete clothing item: $id", e)
            }
        }
    }

    override suspend fun ingestStarterPack(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getStarterPack()
            response.clothing.forEach { dto ->
                val item = ClothingItem(
                    name = dto.name,
                    brand = "KoColor",
                    category = try { ClothingCategory.valueOf(dto.macroCategory.uppercase()) } catch (e: Exception) { ClothingCategory.OTHER },
                    colorHex = dto.colorHex,
                    imageUrl = dto.imageUrl
                )
                saveClothingItem(item)
            }
        }
    }

    private suspend fun analyzeGarment(item: ClothingItem): ClothingItem {
        val imagePath = item.imageUrl ?: return item
        return try {
            val uri = if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
                Uri.parse(imagePath)
            } else {
                Uri.fromFile(File(imagePath))
            }
            
            val bitmap = loadDownsampledBitmap(uri) ?: return item
            colorEngine.processGarment(bitmap, item)
        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed for item: ${item.name}", e)
            item
        }
    }

    private fun loadDownsampledBitmap(uri: Uri): Bitmap? {
        return try {
            val openStream = {
                if (uri.scheme == "content") {
                    context.contentResolver.openInputStream(uri)
                } else {
                    val path = uri.path ?: ""
                    FileInputStream(path)
                }
            }
            
            val inputStream = openStream() ?: return null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            val targetWidth = 400
            val targetHeight = 400
            
            var inSampleSize = 1
            if (options.outHeight > targetHeight || options.outWidth > targetWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= targetHeight && halfWidth / inSampleSize >= targetWidth) {
                    inSampleSize *= 2
                }
            }

            val finalOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val finalStream = openStream() ?: return null
            val bitmap = BitmapFactory.decodeStream(finalStream, null, finalOptions)
            finalStream.close()
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load downsampled bitmap: $uri", e)
            null
        }
    }
}
