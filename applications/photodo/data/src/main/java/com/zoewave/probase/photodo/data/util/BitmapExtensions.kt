package com.zoewave.probase.photodo.data.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import com.google.android.gms.wearable.Asset
import java.io.ByteArrayOutputStream

/**
 * Optimized image compression for Wear OS Data Layer sync.
 * Scales to max 150px, converts to grayscale, and compresses to WEBP.
 */
fun Bitmap.toTinyGrayscaleAsset(): Asset {
    val maxSize = 150
    val width = width
    val height = height

    // 1. Calculate new dimensions preserving aspect ratio
    val (newWidth, newHeight) = if (width > height) {
        val scale = maxSize.toFloat() / width
        maxSize to (height * scale).toInt()
    } else {
        val scale = maxSize.toFloat() / height
        (width * scale).toInt() to maxSize
    }

    // 2. Create a grayscale bitmap
    val grayscaleBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(grayscaleBitmap)
    val paint = Paint()
    val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    
    // Scale and draw grayscale
    val srcRect = android.graphics.Rect(0, 0, width, height)
    val dstRect = android.graphics.Rect(0, 0, newWidth, newHeight)
    canvas.drawBitmap(this, srcRect, dstRect, paint)

    // 3. Compress to WEBP (or JPEG as fallback)
    val stream = ByteArrayOutputStream()
    val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        @Suppress("DEPRECATION")
        Bitmap.CompressFormat.WEBP
    }
    
    grayscaleBitmap.compress(format, 50, stream)
    val byteArray = stream.toByteArray()
    
    return Asset.createFromBytes(byteArray)
}
