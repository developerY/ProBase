package com.zoewave.probase.seaweed.mobile.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.model.CategoryOverview
import kotlin.math.absoluteValue

@Composable
fun CategoryQuickJumpCard(
    category: CategoryOverview,
    onClick: () -> Unit,
    onDelete: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = color.copy(alpha = 0.7f)
                    )
                }
            }

            Column {
                Text(
                    text = "$${CurrencyUtils.formatCents(category.totalAmountCents)}",
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

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(spendingByCategory) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 32f
        val gapDegree = 2f // Degree for segment gaps
        
        // Background ring
        drawCircle(
            color = Color.LightGray.copy(alpha = 0.1f),
            style = Stroke(width = strokeWidth)
        )
        
        var startAngle = -90f
        spendingByCategory.keys.forEachIndexed { index, category ->
            val amount = spendingByCategory[category] ?: 0.0
            val fullSweepAngle = (amount / totalSpending).toFloat() * 360f
            
            // Apply animation and leave a small gap for segments
            val sweepAngle = (fullSweepAngle * animationProgress.value)
            
            if (sweepAngle > gapDegree) {
                drawArc(
                    color = categoryColors[index % categoryColors.size],
                    startAngle = startAngle + (gapDegree / 2f),
                    sweepAngle = sweepAngle - gapDegree,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            startAngle += fullSweepAngle
        }
    }
}

val categoryColors = listOf(
    Color(0xFF6750A4), // Deep Purple
    Color(0xFF006C4C), // Emerald Green
    Color(0xFFB3261E), // Deep Red
    Color(0xFF2196F3), // Bright Blue
    Color(0xFFFF9800), // Amber
    Color(0xFF009688), // Teal
    Color(0xFFE91E63), // Pink
    Color(0xFF9C27B0)  // Purple
)

private val String.absoluteValue: Int
    get() = if (this.hashCode() == Int.MIN_VALUE) 0 else Math.abs(this.hashCode())

@Preview(showBackground = true)
@Composable
private fun CategoryQuickJumpCardPreview() {
    MaterialTheme {
        CategoryQuickJumpCard(
            category = CategoryOverview("shopping_id", "Shopping", 25000L, 5, 50000L, 25000L, 0.5f),
            onClick = {},
            onDelete = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
