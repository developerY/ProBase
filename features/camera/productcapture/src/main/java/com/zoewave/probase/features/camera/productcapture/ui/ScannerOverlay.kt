package com.zoewave.probase.features.camera.productcapture.ui

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * A UI-driven targeting overlay for the "12-Megapixel Diet".
 * It darkens the background and punches out a clear ROI for the user to align the product.
 */
@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    onBoundsCalculated: (Rect) -> Unit
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Define a tall ROI rectangle (ideal for bottles and boxes)
        val rectWidth = canvasWidth * 0.75f
        val rectHeight = canvasHeight * 0.55f 
        
        val left = (canvasWidth - rectWidth) / 2f
        val top = (canvasHeight - rectHeight) / 2f
        val right = left + rectWidth
        val bottom = top + rectHeight

        val androidRect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        
        // Use a side effect to pass bounds back to the owner
        onBoundsCalculated(androidRect)

        val overlayPath = Path().apply {
            addRect(ComposeRect(0f, 0f, canvasWidth, canvasHeight))
            addRoundRect(
                RoundRect(
                    left = left, top = top, right = right, bottom = bottom,
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            )
            fillType = PathFillType.EvenOdd
        }

        // 1. Draw the Dimmed Background
        drawPath(
            path = overlayPath,
            color = Color.Black.copy(alpha = 0.7f)
        )

        // 2. Draw the targeting frame
        drawRoundRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            cornerRadius = CornerRadius(24.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
