package com.zoewave.probase.core.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

data class BarData(
    val label: String,
    val value: Double,
    val color: Color? = null
)

@Composable
fun SimpleBarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier,
    onBarClick: ((BarData) -> Unit)? = null,
    selectedBar: BarData? = null,
    barColor: Color = MaterialTheme.colorScheme.primary,
    selectedBarColor: Color = MaterialTheme.colorScheme.secondary,
    height: Int = 200
) {
    if (data.isEmpty()) return

    val maxValue = remember(data) { data.maxOf { it.value }.coerceAtLeast(1.0) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .padding(horizontal = 8.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val canvasWidth = size.width
                        val barWidthWithSpacing = canvasWidth / data.size
                        val barWidth = barWidthWithSpacing * 0.7f
                        val spacing = barWidthWithSpacing * 0.3f
                        
                        data.forEachIndexed { index, barData ->
                            val left = index * barWidthWithSpacing + (spacing / 2)
                            val barHeight = (barData.value / maxValue).toFloat() * size.height
                            val top = size.height - barHeight
                            
                            val rect = Rect(left, top, left + barWidth, size.height.toFloat())
                            if (rect.contains(offset)) {
                                onBarClick?.invoke(barData)
                            }
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = (canvasWidth / data.size) * 0.7f
            val spacing = (canvasWidth / data.size) * 0.3f
            
            data.forEachIndexed { index, barData ->
                val barHeight = (barData.value / maxValue).toFloat() * canvasHeight
                val left = index * (barWidth + spacing) + (spacing / 2)
                val top = canvasHeight - barHeight
                val isSelected = barData == selectedBar
                
                drawRoundRect(
                    color = if (isSelected) selectedBarColor else (barData.color ?: barColor),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                
                // Draw Value Label
                if (barHeight > 20.dp.toPx()) {
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color = if (isSelected) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        canvas.nativeCanvas.drawText(
                            String.format(Locale.getDefault(), "$%.0f", barData.value),
                            left + barWidth / 2,
                            top + 15.dp.toPx(),
                            paint
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.forEach { barData ->
                Text(
                    text = barData.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
