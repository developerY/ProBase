package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.model.KoColorRoute

data class GroupSectionUiState(
    val title: String,
    val itemCount: Int,
    val isExpanded: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSectionCard(
    uiState: GroupSectionUiState,
    modifier: Modifier = Modifier,
    onEvent: (Boolean) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        onClick = { onEvent(!uiState.isExpanded) }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Category, null, tint = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = uiState.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(text = stringResource(R.string.applications_kocolor_features_cosmetics_item_count_format, uiState.itemCount), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Icon(
                imageVector = if (uiState.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
    }
}
