package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    
    // Default hero image if no product image
    val heroImageUrl = linkedProduct?.imageUrl ?: "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&q=80&w=800"

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9F7F2)), 
        contentPadding = PaddingValues(24.dp), 
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFF1F3F0))
            ) {
                AsyncImage(
                    model = heroImageUrl, 
                    contentDescription = null, 
                    modifier = Modifier.fillMaxSize(), 
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)), startY = 400f)))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(32.dp)) {
                    Text(
                        text = "STAGE ${step.layeringOrder + 1}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.White.copy(alpha = 0.8f), 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = step.title, 
                        style = MaterialTheme.typography.displayMedium, 
                        color = Color.White, 
                        fontFamily = FontFamily.Serif, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val subtitle = step.subtitle
        if (subtitle != null) {
            item {
                Text(
                    text = subtitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
            }
        }

        if (step.description.isNotBlank()) {
            item {
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF1A1A1A)
                )
            }
        }

        val actionLabel = step.actionLabel
        if (actionLabel != null) {
            item {
                Surface(
                    color = Color.White.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "YOUR ACTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }

        if (linkedProduct != null) {
            item {
                Text(
                    text = "LINKED PRODUCT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(16.dp))
                Surface(color = Color.White, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))) {
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
            Button(
                onClick = { onEvent(Unit) }, 
                modifier = Modifier.fillMaxWidth().height(64.dp), 
                shape = RoundedCornerShape(16.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Icon(Icons.Rounded.EditNote, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("EDIT RITUAL STAGE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
        
        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun StepHeroPagePreview() {
    MaterialTheme {
        StepHeroPage(uiState = RoutineStep(id = "1", title = "Step", layeringOrder = 0) to emptyList(), onEvent = {})
    }
}
