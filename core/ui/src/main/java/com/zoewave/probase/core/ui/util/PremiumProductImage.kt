package com.zoewave.probase.core.ui.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter

/**
 * A high-fidelity image loader that uses a "Breathing Blur" effect.
 * It decodes a BlurHash string and applies a subtle alpha pulse animation
 * while the high-resolution asset is fetching from the CDN.
 * 
 * NOTE: Switched to AsyncImage (non-subcompose) to support intrinsic measurements
 * required by layouts like Row(weight) and FlowRow.
 */
@Composable
fun PremiumProductImage(
    imageUrl: String?,
    blurHash: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackColor: Color = Color.Transparent
) {
    // 1. Asynchronously decode the BlurHash into a Painter
    val blurPainter = rememberBlurHashPainter(blurHash = blurHash)

    // 2. Set up the efficient infinite breathing (pulse) animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    var isLoaded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            onState = { state ->
                isLoaded = state is AsyncImagePainter.State.Success
            }
        )

        if (!isLoaded) {
            // -- LOADING/ERROR STATE (Placeholder Overlay) --
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fallbackColor.copy(alpha = 0.1f))
            ) {
                if (blurPainter != null) {
                    Image(
                        painter = blurPainter,
                        contentDescription = "Loading placeholder",
                        contentScale = contentScale,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(alphaAnim) 
                    )
                } else if (fallbackColor != Color.Transparent) {
                    // Fallback to solid pulse if no blurhash
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(alphaAnim * 0.5f)
                            .background(fallbackColor)
                    )
                }
            }
        }
    }
}
