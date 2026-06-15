package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.core.model.ritual.RoutineStep

@Composable
fun SplitRitualStep(
    uiState: Triple<RoutineStep, CosmeticItem?, Boolean>,
    onEvent: (Unit) -> Unit,
    onInfoClick: (RoutineStep) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val (step, linkedProduct, isReorderMode) = uiState
    val isCompleted = step.isCompleted
    val hasAmountInfo = linkedProduct?.amountPerUse != null && linkedProduct.amountRemaining != null
    
    val backgroundColor = if (isCompleted) Color(0xFFE5E7E1) else if (!hasAmountInfo) Color(0xFFF5F5F5) else Color.White
    val iconColor = if (isCompleted) Color(0xFF5A5F4B) else MaterialTheme.colorScheme.outlineVariant

    val fillLevel = linkedProduct?.fillLevel ?: 1.0
    val statusColor = when {
        !hasAmountInfo -> Color.Gray.copy(alpha = 0.3f)
        fillLevel > 0.5 -> Color(0xFF4CAF50) // Green
        fillLevel > 0.2 -> Color(0xFFFFA000) // Orange
        else -> Color(0xFFD32F2F) // Red
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = if (!isCompleted && hasAmountInfo) BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)) else null
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT ZONE: Mark Done
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(enabled = !isReorderMode, onClick = { onEvent(Unit) })
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isReorderMode) {
                    Icon(
                        Icons.Default.DragHandle, 
                        null, 
                        modifier = Modifier.size(24.dp).alpha(0.3f)
                    )
                } else {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            color = if (isCompleted) iconColor else Color.Transparent,
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp).border(1.5.dp, iconColor, CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isCompleted) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        
                        Surface(
                            color = statusColor,
                            shape = CircleShape,
                            modifier = Modifier.size(10.dp).border(1.dp, Color.White, CircleShape)
                        ) {}
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(vertical = 16.dp),
                color = if (isCompleted) Color.Black.copy(alpha = 0.05f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )

            // RIGHT ZONE: Info / Knowledge Hub
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = { navTo(KoColorRoute.Back) }) // Triggers Knowledge Hub navigation via parent lambda
                    .padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).alpha(if (hasAmountInfo) 1f else 0.5f)) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(if (isCompleted) 0.6f else 1f)
                    )
                    Text(
                        text = if (hasAmountInfo) "${(fillLevel * 100).toInt()}% Remaining" else "Missing consumption data",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasAmountInfo) statusColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                IconButton(onClick = { onInfoClick(step) }) {
                    Icon(
                        imageVector = Icons.Default.Info, 
                        contentDescription = "Scientific Info",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplitRitualStepPreview() {
    MaterialTheme {
        SplitRitualStep(
            uiState = Triple(RoutineStep(id = "1", title = "Step", layeringOrder = 0), null, false),
            onEvent = {},
            onInfoClick = {},
            navTo = {}
        )
    }
}
