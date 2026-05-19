package com.probase.kocolor.features.color

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.palette.graphics.Palette
import java.io.InputStream

class WardrobeAnalyzer(private val context: Context) {
    data class RawAnalysisResult(
        val dominantHex: String,
        val vibrantHex: String?,
        val mutedHex: String?,
        val allSwatches: List<String>
    )

    fun analyzeImage(imageUri: Uri): RawAnalysisResult? {
        val rawBitmap = loadDownsampledBitmap(imageUri, 400, 400) ?: return null
        return try {
            val palette = Palette.from(rawBitmap).generate()
            val dominantSwatch = palette.swatches.maxByOrNull { it.population }
            val dominantHex = dominantSwatch?.rgb?.let { ColorScienceUtils.colorToHex(it) } ?: "#FFFFFF"
            val vibrantHex = palette.getVibrantColor(0).takeIf { it != 0 }?.let { ColorScienceUtils.colorToHex(it) }
            val mutedHex = palette.getMutedColor(0).takeIf { it != 0 }?.let { ColorScienceUtils.colorToHex(it) }
            val uniqueHexes = palette.swatches.sortedByDescending { it.population }.take(6).map { ColorScienceUtils.colorToHex(it.rgb) }

            RawAnalysisResult(dominantHex, vibrantHex, mutedHex, uniqueHexes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            rawBitmap.recycle()
        }
    }

    private fun loadDownsampledBitmap(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        var inputStream: InputStream? = context.contentResolver.openInputStream(uri) ?: return null
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            options.apply {
                inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                inJustDecodeBounds = false
            }
            inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
