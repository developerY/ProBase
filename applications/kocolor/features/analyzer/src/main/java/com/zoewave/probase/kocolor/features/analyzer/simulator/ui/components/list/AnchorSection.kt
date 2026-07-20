package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ColorFamily

@Composable
fun <T> AnchorSection(
    title: String,
    categories: List<Triple<String, ImageVector, Any>>,
    selectedCategory: Any,
    onCategorySelect: (Any) -> Unit,
    families: Map<ColorFamily, List<T>>,
    anchoredFamily: ColorFamily?,
    onToggle: (ColorFamily) -> Unit,
    emptyMessage: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color.Black
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium, // Adjust as needed
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { (name, icon, cat) ->
                        val isSelected = selectedCategory == cat
                        CategoryPill(
                            label = name,
                            icon = icon,
                            isSelected = isSelected,
                            onClick = { onCategorySelect(cat) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (families.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ColorFamily.entries.filter { families.containsKey(it) }.forEach { family ->
                            val isSelected = anchoredFamily == family

                            item {
                                ColorFamilySwatch(
                                    family = family,
                                    isSelected = isSelected,
                                    onClick = { onToggle(family) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnchorSectionPreview() {
    AnchorSection(
        title = "CLOTHING ANCHORS",
        categories = listOf(
            Triple("Top", Icons.Default.Checkroom, "TOP"),
            Triple("Bottom", Icons.Default.Layers, "BOTTOM")
        ),
        selectedCategory = "TOP",
        onCategorySelect = {},
        families = mapOf(ColorFamily.TRUE_RED to listOf("Red Item")),
        anchoredFamily = ColorFamily.TRUE_RED,
        onToggle = {},
        emptyMessage = "No items found"
    )
}
