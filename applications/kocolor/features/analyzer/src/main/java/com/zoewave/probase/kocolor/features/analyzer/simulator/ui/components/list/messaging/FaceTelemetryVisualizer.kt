package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import android.graphics.PointF
import android.graphics.Rect
import android.net.Uri
import android.util.Log
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
        val model = if (!imageUri.isNullOrEmpty() && imageUri.startsWith("file://")) {
            Uri.parse(imageUri)
        } else {
            imageUri
        }
        ImageRequest.Builder(context)
            .data(model)
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
            val imgW = if (imageW > 0f) imageW else size.width
            val imgH = if (imageH > 0f) imageH else size.height

            val scale = minOf(size.width / imgW, size.height / imgH)
            val offsetX = (size.width - (imgW * scale)) / 2f
            val offsetY = (size.height - (imgH * scale)) / 2f

            fun mapX(x: Float): Float = (x * scale) + offsetX
            fun mapY(y: Float): Float = (y * scale) + offsetY
            fun scalePoint(p: PointF): Offset = Offset(mapX(p.x), mapY(p.y))

            val faceBox = telemetry.faceBoundingBox ?: Rect(
                (imgW * 0.20f).toInt(),
                (imgH * 0.22f).toInt(),
                (imgW * 0.80f).toInt(),
                (imgH * 0.72f).toInt()
            )

            val hairBox = telemetry.hairBoundingBox ?: Rect(
                (imgW * 0.20f).toInt(),
                (imgH * 0.12f).toInt(),
                (imgW * 0.80f).toInt(),
                (imgH * 0.22f).toInt()
            )

            val cheekPoint = telemetry.cheekPoint ?: PointF(
                faceBox.centerX() - faceBox.width() * 0.18f,
                faceBox.centerY() + faceBox.height() * 0.08f
            )

            val eyePoint = telemetry.eyePoint ?: PointF(
                faceBox.centerX() - faceBox.width() * 0.18f,
                faceBox.centerY() - faceBox.height() * 0.12f
            )

            val rightCheekPoint = PointF(
                faceBox.centerX() + faceBox.width() * 0.18f,
                faceBox.centerY() + faceBox.height() * 0.08f
            )

            Log.d("FaceTelemetryVisualizer", "Drawing Visualizer: imgW=$imgW, imgH=$imgH, canvasSize=${size.width}x${size.height}, faceBox=$faceBox, cheek=$cheekPoint, eye=$eyePoint")

            // 1. Hair Bounding Box (Yellow Dashed)
            drawRect(
                color = Color.Yellow.copy(alpha = 0.85f),
                topLeft = Offset(mapX(hairBox.left.toFloat()), mapY(hairBox.top.toFloat())),
                size = Size(hairBox.width() * scale, hairBox.height() * scale),
                style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
            )

            // 2. Face Bounding Box (Cyan Dashed)
            drawRoundRect(
                color = Color.Cyan.copy(alpha = 0.75f),
                topLeft = Offset(mapX(faceBox.left.toFloat()), mapY(faceBox.top.toFloat())),
                size = Size(faceBox.width() * scale, faceBox.height() * scale),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
            )

            // 3. Left Cheek Node (Cyan Target Circle)
            drawCircle(
                color = Color.Cyan,
                radius = 14f,
                center = scalePoint(cheekPoint),
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.35f),
                radius = 28f,
                center = scalePoint(cheekPoint)
            )

            // 4. Right Cheek Node (Dodger Blue Target Circle)
            drawCircle(
                color = Color(0xFF1E88E5),
                radius = 14f,
                center = scalePoint(rightCheekPoint),
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = Color(0xFF1E88E5).copy(alpha = 0.35f),
                radius = 28f,
                center = scalePoint(rightCheekPoint)
            )

            // 5. Eye Node (Magenta Target Circle)
            drawCircle(
                color = Color.Magenta,
                radius = 14f,
                center = scalePoint(eyePoint),
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = Color.Magenta.copy(alpha = 0.35f),
                radius = 28f,
                center = scalePoint(eyePoint)
            )
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

