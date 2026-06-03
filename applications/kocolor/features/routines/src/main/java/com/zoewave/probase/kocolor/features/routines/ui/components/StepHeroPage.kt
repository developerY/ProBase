package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.RoutineStep

@Composable
fun StepHeroPage(uiState: Pair<RoutineStep, List<CosmeticItem>>, onEvent: (Unit) -> Unit) {
    val (step, allProducts) = uiState
    val linkedProduct = allProducts.find { step.productIds.contains(it.id) }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(32.dp)).background(Color(0xFFF1F3F0))) {
                if (linkedProduct?.imageUrl != null) AsyncImage(model = linkedProduct.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)), startY = 400f)))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    Text(text = "STAGE ${step.layeringOrder + 1}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text(text = step.title, style = MaterialTheme.typography.headlineLarge, color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (linkedProduct != null) {
            item {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(20.dp)) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            if (linkedProduct.imageUrl != null) AsyncImage(model = linkedProduct.imageUrl, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(text = linkedProduct.brand.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = linkedProduct.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                            Text(text = linkedProduct.microCategory.displayName, style = MaterialTheme.typography.labelSmall, modifier = Modifier.alpha(0.5f))
                        }
                    }
                }
            }
        }
        item {
            Button(onClick = { onEvent(Unit) }, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)) {
                Icon(Icons.Default.Tune, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("EDIT RITUAL STAGE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StepHeroPagePreview() {
    MaterialTheme {
        StepHeroPage(uiState = RoutineStep(id = "1", title = "Step", layeringOrder = 0) to emptyList(), onEvent = {})
    }
}
