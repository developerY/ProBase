package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
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
import com.zoewave.probase.kocolor.features.routines.ui.RoutinesEvent
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.RoutineStep

@Composable
fun StepHeroPage(
    uiState: Triple<RoutineStep, List<CosmeticItem>, Long>, 
    onEvent: (RoutinesEvent) -> Unit,
    onEditStage: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val (step, allProducts, routineId) = uiState
    val linkedProducts = allProducts.filter { step.productIds.contains(it.id) }
    
    // Default hero image if no product image
    val heroImageUrl = linkedProducts.firstOrNull()?.imageUrl ?: "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&q=80&w=800"

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

        // --- PROGRESS PHOTOS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROGRESS PHOTOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = { navTo(KoColorRoute.Camera("ritual_step:$routineId:${step.id}")) }) {
                        Icon(Icons.Rounded.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (step.photoUris.isEmpty()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("No progress photos captured yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(step.photoUris) { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { onEvent(RoutinesEvent.RemoveStepPhoto(routineId, step.id, uri)) },
                                    modifier = Modifier.align(Alignment.TopEnd).size(32.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(Icons.Rounded.Delete, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- PERSONAL NOTES ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "PERSONAL NOTES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = step.notes,
                    onValueChange = { onEvent(RoutinesEvent.UpdateStepNote(routineId, step.id, it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Log whatever...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.8f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.4f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.05f)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif),
                    minLines = 3
                )
            }
        }

        if (linkedProducts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "LINKED PRODUCTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    linkedProducts.forEach { linkedProduct ->
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
            }
        }

        item {
            Button(
                onClick = { onEditStage() }, 
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
        StepHeroPage(uiState = Triple(RoutineStep(id = "1", title = "Step", layeringOrder = 0), emptyList(), 1L), onEvent = {}, onEditStage = {}, navTo = {})
    }
}
