package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class AnalyticsStatUiState(
    val label: String,
    val value: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsStatCard(
    uiState: AnalyticsStatUiState,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        onClick = onEvent,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(uiState.icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(text = uiState.value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(text = uiState.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
private fun AnalyticsStatCardPreview() {
    MaterialTheme {
        AnalyticsStatCard(
            uiState = AnalyticsStatUiState(label = "TOTAL USES", value = "165", icon = Icons.Default.History)
        )
    }
}
