package com.zoewave.probase.kocolor.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.zoewave.probase.kocolor.data.engine.WardrobeColorEngine
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.model.ClothingItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WardrobeRepository"

@Singleton
class WardrobeRepository @Inject constructor(
    private val clothingDao: ClothingDao,
    private val colorEngine: WardrobeColorEngine,
    @ApplicationContext private val context: Context
) {

    fun getAllClothing(): Flow<List<ClothingItem>> {
        return clothingDao.getAllClothing()
            .map { entities -> entities.map { it.toModel() } }
            .catch { e ->
                Log.e(TAG, "Error fetching all clothing items", e)
                emit(emptyList())
            }
    }

    fun getClothingById(id: Long): Flow<ClothingItem?> {
        return clothingDao.getClothingById(id)
            .map { it?.toModel() }
            .catch { e ->
                Log.e(TAG, "Error fetching clothing item by id: $id", e)
                emit(null)
            }
    }

    /**
     * Saves a garment and automatically triggers the analytical color pipeline.
     */
    suspend fun saveClothingItem(item: ClothingItem) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Saving clothing item: ${item.name} (id: ${item.id}, image: ${item.imageUrl})")
        try {
            val existingItem = if (item.id != 0L) clothingDao.getClothingById(item.id).firstOrNull()?.toModel() else null
            
            val needsAnalysis = item.imageUrl != null && (
                item.dominantHex == null || 
                item.imageUrl != existingItem?.imageUrl
            )

            val analyzedItem = if (needsAnalysis) {
                Log.d(TAG, "Item needs analysis (new image or missing data)")
                analyzeGarment(item)
            } else item

            Log.d(TAG, "Inserting into DB: ${analyzedItem.imageUrl}")
            clothingDao.insertClothing(analyzedItem.toEntity())
            Log.d(TAG, "Save complete for: ${item.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save clothing item: ${item.name}", e)
        }
    }

    suspend fun wearClothingItem(id: Long) = withContext(Dispatchers.IO) {
        try {
            clothingDao.getClothingById(id).firstOrNull()?.let { entity ->
                val model = entity.toModel()
                clothingDao.insertClothing(model.copy(usageCount = model.usageCount + 1).toEntity())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log wear for item: $id", e)
        }
    }

    suspend fun deleteClothing(id: Long) = withContext(Dispatchers.IO) {
        try {
            clothingDao.deleteClothing(id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete clothing item: $id", e)
        }
    }

    private suspend fun analyzeGarment(item: ClothingItem): ClothingItem {
        val imagePath = item.imageUrl ?: return item
        return try {
            val uri = if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
                Uri.parse(imagePath)
            } else {
                Uri.fromFile(java.io.File(imagePath))
            }
            
            val bitmap = loadDownsampledBitmap(uri) ?: return item
            colorEngine.processGarment(bitmap, item)
        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed for item: ${item.name}", e)
            item
        }
    }

    private fun loadDownsampledBitmap(uri: Uri): Bitmap? {
        Log.d(TAG, "Loading bitmap for analysis: $uri (scheme: ${uri.scheme}, path: ${uri.path})")
        return try {
            val openStream = {
                if (uri.scheme == "content") {
                    context.contentResolver.openInputStream(uri)
                } else {
                    val path = uri.path ?: ""
                    Log.d(TAG, "Opening FileInputStream for path: $path")
                    java.io.FileInputStream(path)
                }
            }
            
            val inputStream = openStream() ?: run {
                Log.e(TAG, "Could not open input stream for URI: $uri")
                return null
            }
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                Log.e(TAG, "Bitmap bounds are invalid: ${options.outWidth}x${options.outHeight}")
                return null
            }
            
            Log.d(TAG, "Original image size: ${options.outWidth}x${options.outHeight}")

            // Target dimensions for analysis (speed vs accuracy)
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

            Log.d(TAG, "Using inSampleSize: $inSampleSize")

            val finalOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val finalStream = openStream() ?: return null
            val bitmap = BitmapFactory.decodeStream(finalStream, null, finalOptions)
            finalStream.close()
            
            if (bitmap != null) {
                Log.d(TAG, "Successfully loaded bitmap: ${bitmap.width}x${bitmap.height}")
            } else {
                Log.e(TAG, "Bitmap decoding failed for URI: $uri")
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load downsampled bitmap: $uri", e)
            null
        }
    }
}
