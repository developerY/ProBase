package com.zoewave.probase.kocolor.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.data.engine.WardrobeColorEngine
import com.zoewave.probase.kocolor.model.ClothingItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeRepository @Inject constructor(
    private val clothingDao: ClothingDao,
    private val colorEngine: WardrobeColorEngine,
    @ApplicationContext private val context: Context
) {

    fun getAllClothing(): Flow<List<ClothingItem>> {
        return clothingDao.getAllClothing().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getClothingById(id: Long): Flow<ClothingItem?> {
        return clothingDao.getClothingById(id).map { it?.toModel() }
    }

    /**
     * Saves a garment and automatically triggers the analytical color pipeline.
     */
    suspend fun saveClothingItem(item: ClothingItem) = withContext(Dispatchers.IO) {
        val analyzedItem = if (item.imageUrl != null && item.dominantHex == null) {
            analyzeGarment(item)
        } else item

        clothingDao.insertClothing(analyzedItem.toEntity())
    }

    suspend fun deleteClothing(id: Long) {
        clothingDao.deleteClothing(id)
    }

    private suspend fun analyzeGarment(item: ClothingItem): ClothingItem {
        val uri = Uri.parse(item.imageUrl)
        val bitmap = loadDownsampledBitmap(uri) ?: return item
        return colorEngine.processGarment(bitmap, item)
    }

    private fun loadDownsampledBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

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
            null
        }
    }

    private fun ClothingItemEntity.toModel() = ClothingItem(
        id = id,
        name = name,
        brand = brand,
        category = category,
        colorHex = colorHex,
        size = size,
        material = material,
        price = price,
        imageUrl = imageUrl,
        notes = notes,
        timestamp = timestamp,
        dominantHex = dominantHex,
        vibrantHex = vibrantHex,
        mutedHex = mutedHex,
        paletteHexes = paletteHexes,
        colorTemperature = colorTemperature,
        seasonalPalette = seasonalPalette,
        contrastLevel = contrastLevel,
        koColorGroup = koColorGroup
    )

    private fun ClothingItem.toEntity() = ClothingItemEntity(
        id = id,
        name = name,
        brand = brand,
        category = category,
        colorHex = colorHex,
        size = size,
        material = material,
        price = price,
        imageUrl = imageUrl,
        notes = notes,
        timestamp = timestamp,
        dominantHex = dominantHex,
        vibrantHex = vibrantHex,
        mutedHex = mutedHex,
        paletteHexes = paletteHexes,
        colorTemperature = colorTemperature,
        seasonalPalette = seasonalPalette,
        contrastLevel = contrastLevel,
        koColorGroup = koColorGroup
    )
}
