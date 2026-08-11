package com.zoewave.probase.core.ui.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
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
fun rememberBlurHashPainter(blurHash: String?, width: Int = 32, height: Int = 32): Painter? {
    if (blurHash == null) return null
    
    val context = LocalContext.current
    
    // Produce state runs asynchronously. It starts null and updates when the Bitmap is ready.
    val bitmapState = produceState<Bitmap?>(initialValue = null, blurHash) {
        value = withContext(Dispatchers.Default) {
            BlurHashDecoder.decode(blurHash, width, height)
        }
    }

    // Convert the native Bitmap to a Compose-friendly Painter
    return bitmapState.value?.let { bitmap ->
        val drawable = BitmapDrawable(context.resources, bitmap)
        rememberDrawablePainter(drawable = drawable)
    }
}
