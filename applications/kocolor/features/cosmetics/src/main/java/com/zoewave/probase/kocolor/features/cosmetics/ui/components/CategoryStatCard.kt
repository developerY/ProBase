package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CategoryStatUiState(
    val title: String,
    val value: String,
    val isAlert: Boolean = false
)

@Composable
fun CategoryStatCard(
    uiState: CategoryStatUiState, 
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (uiState.isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) 
                          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (uiState.isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.alpha(0.5f),
                color = contentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = uiState.value, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold, 
                maxLines = 1,
                color = contentColor
            )
        }
    }
}
