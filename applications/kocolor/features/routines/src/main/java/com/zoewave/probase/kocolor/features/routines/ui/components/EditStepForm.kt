package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.RoutineStep

@Composable
fun EditStepForm(uiState: Pair<RoutineStep, List<CosmeticItem>>, onEvent: (String) -> Unit) {
    val (step, allProducts) = uiState
    val linkedProduct = allProducts.find { step.productIds.contains(it.id) }
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Step Title", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, modifier = Modifier.alpha(0.4f), letterSpacing = 1.sp)
            Text(text = step.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.alpha(0.1f))
        }
        Column(modifier = Modifier.clickable { onEvent("product") }.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Product Used", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, modifier = Modifier.alpha(0.4f), letterSpacing = 1.sp)
            Text(text = linkedProduct?.name ?: "Select a product...", style = MaterialTheme.typography.headlineSmall, color = if (linkedProduct == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            HorizontalDivider(modifier = Modifier.alpha(0.1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditStepFormPreview() {
    MaterialTheme {
        EditStepForm(uiState = RoutineStep(id = "1", title = "Step", layeringOrder = 0) to emptyList(), onEvent = {})
    }
}
