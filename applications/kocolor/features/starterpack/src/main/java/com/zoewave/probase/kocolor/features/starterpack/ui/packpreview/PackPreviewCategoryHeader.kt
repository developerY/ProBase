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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val lavender = Color(0xFFE6E0F0) // Soft lavender background
    val purpleText = Color(0xFF745E7A)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = lavender,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp), // Match image's rounded top
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        contentDescription = "Expand/Collapse",
                        tint = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${categoryName.uppercase()} ($selectedCount/$totalCount)",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Select All",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = purpleText,
                        modifier = Modifier.clickable { onSelectAll() }
                    )
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = purpleText,
                        modifier = Modifier.clickable { onClear() }
                    )
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
