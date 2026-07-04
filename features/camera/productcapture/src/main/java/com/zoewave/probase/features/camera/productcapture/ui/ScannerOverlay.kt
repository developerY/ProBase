package com.zoewave.probase.features.camera.productcapture.ui

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * A UI-driven targeting overlay for the "12-Megapixel Diet".
 * Displays only the targeting frame (bounding box) without any dimmed background.
 */
@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    onBoundsCalculated: (Rect) -> Unit
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Define a "Full Height and Narrow" ROI rectangle
        val rectWidth = canvasWidth * 0.62f // Narrow for better focus on product label
        val rectHeight = canvasHeight * 0.96f // Maximum vertical coverage
        
        val left = (canvasWidth - rectWidth) / 2f
        val top = (canvasHeight - rectHeight) / 2f
        val right = left + rectWidth
        val bottom = top + rectHeight

        val androidRect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        
        // Pass the physical UI coordinates back for the "12-Megapixel Diet" cropping
        onBoundsCalculated(androidRect)

        // 2. Draw ONLY the targeting frame (No black background/mask)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            cornerRadius = CornerRadius(12.dp.toPx()), // Slimmer corners for narrow frame
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
