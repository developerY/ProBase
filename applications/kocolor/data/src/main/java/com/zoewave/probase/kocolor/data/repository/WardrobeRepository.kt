package com.zoewave.probase.kocolor.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.data.engine.WardrobeColorEngine
import com.zoewave.probase.kocolor.model.ClothingItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
        try {
            val analyzedItem = if (item.imageUrl != null && item.dominantHex == null) {
                analyzeGarment(item)
            } else item

            clothingDao.insertClothing(analyzedItem.toEntity())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save clothing item: ${item.name}", e)
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
        return try {
            val uri = Uri.parse(item.imageUrl)
            val bitmap = loadDownsampledBitmap(uri) ?: return item
            colorEngine.processGarment(bitmap, item)
        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed for item: ${item.name}", e)
            item
        }
    }

    private fun loadDownsampledBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

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

            val finalOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val finalStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(finalStream, null, finalOptions)
            finalStream?.close()
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load downsampled bitmap: $uri", e)
            null
        }
    }
}
