package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.FaceTelemetryData
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FaceTelemetryVisualizer(
    imageUri: String?,
    telemetry: FaceTelemetryData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRequest = remember(imageUri) {
        ImageRequest.Builder(context)
            .data(imageUri)
            .crossfade(true)
            .build()
    }

    val imageW = telemetry.imageWidth.toFloat()
    val imageH = telemetry.imageHeight.toFloat()
    val imageAspectRatio = if (imageW > 0f && imageH > 0f) imageW / imageH else 3f / 4f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(imageAspectRatio)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) { 
        AsyncImage(
            model = imageRequest,
            contentDescription = "Analyzed Portrait",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (imageW <= 0f || imageH <= 0f) return@Canvas

            // Uniform scale factor ensuring X and Y scale identically
            val scale = minOf(size.width / imageW, size.height / imageH)
            val offsetX = (size.width - (imageW * scale)) / 2f
            val offsetY = (size.height - (imageH * scale)) / 2f

            fun scalePoint(p: PointF): Offset {
                return Offset(
                    x = (p.x * scale) + offsetX,
                    y = (p.y * scale) + offsetY
                )
            }

            // Hair Bounding Box
            telemetry.hairBoundingBox?.let { rect ->
                drawRect(
                    color = Color.Yellow.copy(alpha = 0.6f),
                    topLeft = Offset((rect.left * scale) + offsetX, (rect.top * scale) + offsetY),
                    size = Size(rect.width() * scale, rect.height() * scale),
                    style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
            }

            // Face Bounding Box
            telemetry.faceBoundingBox?.let { rect ->
                drawRoundRect(
                    color = Color.Cyan.copy(alpha = 0.4f),
                    topLeft = Offset((rect.left * scale) + offsetX, (rect.top * scale) + offsetY),
                    size = Size(rect.width() * scale, rect.height() * scale),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
                )
            }

            // Cheek Node
            telemetry.cheekPoint?.let { point ->
                drawCircle(
                    color = Color.Cyan,
                    radius = 12f,
                    center = scalePoint(point),
                    style = Stroke(width = 6f)
                )
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.3f),
                    radius = 24f,
                    center = scalePoint(point)
                )
            }

            // Eye Node
            telemetry.eyePoint?.let { point ->
                drawCircle(
                    color = Color.Magenta,
                    radius = 12f,
                    center = scalePoint(point),
                    style = Stroke(width = 6f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FaceTelemetryVisualizerPreview() {
    FaceTelemetryVisualizer(
        imageUri = null,
        telemetry = MessagingPreviewData.sampleTelemetry
    )
}

