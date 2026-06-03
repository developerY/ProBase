package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.MacroCategory
import com.zoewave.probase.kocolor.model.MicroCategory

@Composable
fun MacroSelectionPage(onEvent: (MacroCategory) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(MacroCategory.entries) { macro ->
            SelectionRow(text = macro.displayName, onClick = { onEvent(macro) })
        }
    }
}

@Composable
fun MicroSelectionPage(macro: MacroCategory, onEvent: (MicroCategory) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(MicroCategory.entries.filter { it.macro == macro }) { micro ->
            SelectionRow(text = micro.displayName, onClick = { onEvent(micro) })
        }
    }
}

@Composable
fun SelectionRow(text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.alpha(0.3f))
        }
    }
}

@Composable
fun ItemSelectionPage(uiState: Triple<List<CosmeticItem>, List<Long>, (Long) -> Unit>) {
    val (products, selectedIds, onItemClick) = uiState
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(products) { product ->
            val isSelected = selectedIds.contains(product.id)
            Surface(onClick = { onItemClick(product.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.05f))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        if (product.imageUrl != null) AsyncImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(text = product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(text = product.brand, style = MaterialTheme.typography.labelSmall, modifier = Modifier.alpha(0.5f))
                    }
                    Spacer(Modifier.weight(1f))
                    if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
