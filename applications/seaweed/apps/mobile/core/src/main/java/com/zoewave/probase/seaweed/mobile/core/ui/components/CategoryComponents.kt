package com.zoewave.probase.seaweed.mobile.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.seaweed.model.CategoryOverview
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun CategoryQuickJumpCard(
    category: CategoryOverview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorIndex = category.name.hashCode().absoluteValue % categoryColors.size
    val color = categoryColors[colorIndex]

    Card(
        onClick = onClick,
        modifier = modifier
            .width(130.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.0f", category.totalAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = { 1f }, // Placeholder
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun DonutChart(
    spendingByCategory: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val totalSpending = spendingByCategory.values.sum()
    if (totalSpending == 0.0) return

    Canvas(modifier = modifier) {
        val strokeWidth = 24f
        
        // Background ring
        drawCircle(
            color = Color.LightGray.copy(alpha = 0.2f),
            style = Stroke(width = strokeWidth)
        )
        
        var startAngle = -90f
        spendingByCategory.keys.forEachIndexed { index, category ->
            val amount = spendingByCategory[category] ?: 0.0
            val sweepAngle = (amount / totalSpending).toFloat() * 360f
            
            if (sweepAngle > 0) {
                drawArc(
                    color = categoryColors[index % categoryColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            startAngle += sweepAngle
        }
    }
}

val categoryColors = listOf(
    Color(0xFF6750A4), // Purple
    Color(0xFF006C4C), // Green
    Color(0xFFB3261E), // Red
    Color(0xFF625B71), // Muted Purple
    Color(0xFF7D5260), // Muted Red
    Color(0xFF006A6A)  // Teal
)

private val String.absoluteValue: Int
    get() = if (this.hashCode() == Int.MIN_VALUE) 0 else Math.abs(this.hashCode())
