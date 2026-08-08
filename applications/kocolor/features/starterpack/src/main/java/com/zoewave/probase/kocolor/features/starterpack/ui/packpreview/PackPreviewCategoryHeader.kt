package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PackPreviewCategoryHeader(
    categoryName: String,
    selectedCount: Int,
    totalCount: Int,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onSelectAll: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleCollapse() }
            ) {
                Icon(
                    imageVector = if (isCollapsed) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand/Collapse"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$categoryName ($selectedCount/$totalCount)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                TextButton(
                    onClick = onSelectAll,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Select All", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(
                    onClick = onClear,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Clear", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PackPreviewCategoryHeaderPreview() {
    MaterialTheme {
        PackPreviewCategoryHeader(
            categoryName = "PREP",
            selectedCount = 2,
            totalCount = 4,
            isCollapsed = false,
            onToggleCollapse = {},
            onSelectAll = {},
            onClear = {}
        )
    }
}
