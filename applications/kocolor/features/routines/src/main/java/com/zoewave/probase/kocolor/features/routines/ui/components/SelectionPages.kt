package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun MacroSelectionPagePreview() {
    MaterialTheme {
        MacroSelectionPage(uiState = Unit, onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MicroSelectionPagePreview() {
    MaterialTheme {
        MicroSelectionPage(uiState = MicroSelectionUiState(MacroCategory.PREP), onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemSelectionPagePreview() {
    MaterialTheme {
        ItemSelectionPage(
            uiState = ItemSelectionUiState(
                items = listOf(
                    CosmeticItem(id = 1, name = "Product 1", brand = "Brand A", macroCategory = MacroCategory.PREP, microCategory = MicroCategory.CLEANSER, colorHex = "#FFFFFF"),
                    CosmeticItem(id = 2, name = "Product 2", brand = "Brand B", macroCategory = MacroCategory.PREP, microCategory = MicroCategory.TONER, colorHex = "#FFFFFF")
                ),
                selectedIds = listOf(1)
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectionRowPreview() {
    MaterialTheme {
        SelectionRow(uiState = "Skincare & Prep", onEvent = {}, navTo = {})
    }
}

@Composable
fun MacroSelectionPage(
    uiState: Unit,
    modifier: Modifier = Modifier,
    onEvent: (MacroCategory) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(MacroCategory.entries) { macro ->
            SelectionRow(
                uiState = macro.displayName, 
                onEvent = { onEvent(macro) },
                navTo = {}
            )
        }
    }
}

data class MicroSelectionUiState(val macro: MacroCategory)

@Composable
fun MicroSelectionPage(
    uiState: MicroSelectionUiState,
    modifier: Modifier = Modifier,
    onEvent: (MicroCategory) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val macro = uiState.macro
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(MicroCategory.entries.filter { it.macro == macro }) { micro ->
            SelectionRow(
                uiState = micro.displayName, 
                onEvent = { onEvent(micro) },
                navTo = {}
            )
        }
    }
}

@Composable
fun SelectionRow(
    uiState: String, 
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Surface(
        onClick = onEvent, 
        modifier = modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(16.dp), 
        color = MaterialTheme.colorScheme.surface, 
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = uiState, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.alpha(0.3f))
        }
    }
}

data class ItemSelectionUiState(
    val items: List<CosmeticItem>,
    val selectedIds: List<Long>
)

@Composable
fun ItemSelectionPage(
    uiState: ItemSelectionUiState,
    modifier: Modifier = Modifier,
    onEvent: (Long) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val products = uiState.items
    val selectedIds = uiState.selectedIds
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(products) { product ->
            val isSelected = selectedIds.contains(product.id)
            Surface(
                onClick = { onEvent(product.id) }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp), 
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface, 
                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.05f))
            ) {
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
