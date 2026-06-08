package com.zoewave.probase.features.health.nutrition.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.model.RoutineStep

@Composable
fun NutritionRitualStep(
    step: RoutineStep,
    onToggle: () -> Unit,
    onKnowledgeHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = step.isCompleted
    val backgroundColor = if (isCompleted) Color(0xFFE5E7E1) else Color.White
    val iconColor = if (isCompleted) Color(0xFF5A5F4B) else MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = if (!isCompleted) BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)) else null
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT ZONE: Mark Done
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = if (isCompleted) iconColor else Color.Transparent,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp).border(1.5.dp, iconColor, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(vertical = 16.dp),
                color = if (isCompleted) Color.Black.copy(alpha = 0.05f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )

            // RIGHT ZONE: Title & Navigation
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = onKnowledgeHub)
                    .padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(if (isCompleted) 0.6f else 1f)
                    )
                    Text(
                        text = step.subtitle ?: "Metabolic Protocol",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Info, 
                    contentDescription = "Knowledge Hub",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp).padding(end = 8.dp)
                )
            }
        }
    }
}
