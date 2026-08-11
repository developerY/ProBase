package com.zoewave.probase.core.ui.intelligence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.util.CompatibilityResult

/**
 * A high-fidelity UI component that surfaces chemical compatibility alerts.
 */
@Composable
fun CompatibilityBadge(result: CompatibilityResult, modifier: Modifier = Modifier) {
    when (result) {
        is CompatibilityResult.Optimal -> {
            // Luxury design principle: Silence is gold when things work perfectly.
        }
        is CompatibilityResult.PillingWarning -> {
            Surface(
                modifier = modifier.fillMaxWidth(),
                color = Color(0xFFFFF4E5), // Subdued amber background
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Pilling Warning",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Layering Optimization",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            text = result.resolution,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Surface(
    modifier: Modifier,
    color: Color,
    shape: RoundedCornerShape,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color, shape)
    ) {
        content()
    }
}
