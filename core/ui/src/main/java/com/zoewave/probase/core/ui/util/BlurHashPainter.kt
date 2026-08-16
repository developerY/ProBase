package com.zoewave.probase.core.ui.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.asImageBitmap
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Asynchronously decodes a BlurHash string into a Compose Painter.
 * Offloads decoding to Dispatchers.Default to ensure 60fps scrolling.
 */
@Composable
fun rememberBlurHashPainter(
    blurHash: String?, 
    width: Int = 32, 
    height: Int = 32,
    fallbackColor: Color? = null
): Painter? {
    val context = LocalContext.current
    
    // Produce state runs asynchronously. It starts with null.
    val bitmapState = produceState<Bitmap?>(initialValue = null, blurHash) {
        if (blurHash != null) {
            value = withContext(Dispatchers.Default) {
                BlurHashDecoder.decode(blurHash, width, height, punch = 1.2f) // Increased punch for vibrancy
            }
        }
    }

    val bitmap = bitmapState.value
    return if (bitmap != null) {
        val drawable = BitmapDrawable(context.resources, bitmap)
        rememberDrawablePainter(drawable = drawable)
    } else {
        fallbackColor?.let { ColorPainter(it) }
    }
}
