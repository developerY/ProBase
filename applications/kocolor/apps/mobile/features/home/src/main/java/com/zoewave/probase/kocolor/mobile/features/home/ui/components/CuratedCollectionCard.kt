package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.mobile.features.home.ui.CuratedCollectionUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun CuratedCollectionCard(
    uiState: CuratedCollectionUiState,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val analysis = uiState.analysis
    val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm", java.util.Locale.getDefault()) }
    val dateStr = dateFormat.format(java.util.Date(analysis.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onEvent() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFE8E0FD), // Soft Lavender
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = analysis.advice.seasonalType.name.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF673AB7), // Deep Purple
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(
                        onClick = { /* TODO: Duplicate */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    androidx.compose.material3.IconButton(
                        onClick = onEvent,
                        modifier = Modifier.size(24.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    androidx.compose.material3.IconButton(
                        onClick = { /* TODO: Delete */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val heroImage = analysis.advice.clothesUri 
                    ?: analysis.advice.outfitSuggestions.flatMap { it.suggestedItems }.firstOrNull { it.imageUrl != null }?.imageUrl
                    ?: analysis.advice.makeupSuggestions.firstOrNull { it.suggestedProductImageUrl != null }?.suggestedProductImageUrl
                
                if (heroImage != null) {
                    coil.compose.AsyncImage(
                        model = heroImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = analysis.advice.title ?: "The Personal Collection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    var filteredSummary = analysis.advice.summary.removePrefix("Local Architect: ")
                    
                    // 1. Replace Clothing items (w_##) with their actual names
                    analysis.advice.outfitSuggestions.forEach { outfit ->
                        outfit.wardrobeItemIds.forEachIndexed { index, itemId ->
                            val clothingName = outfit.suggestedItems.getOrNull(index)?.name ?: "this garment"
                            val idStr = "w_$itemId"
                            filteredSummary = filteredSummary.replace(Regex("(?i)item\\s+$idStr"), clothingName.lowercase())
                            filteredSummary = filteredSummary.replace("<ITEM:$idStr>", clothingName.lowercase())
                            filteredSummary = filteredSummary.replace(idStr, clothingName.lowercase())
                        }
                    }
                    
                    // 2. Replace Cosmetic items (c_##) with their actual names
                    analysis.advice.makeupSuggestions.forEach { makeupItem ->
                        if (makeupItem.productId != null) {
                            val idStr = "c_${makeupItem.productId}"
                            val name = makeupItem.suggestedProductName ?: "this cosmetic"
                            filteredSummary = filteredSummary.replace(Regex("(?i)item\\s+$idStr"), name.lowercase())
                            filteredSummary = filteredSummary.replace("<ITEM:$idStr>", name.lowercase())
                            filteredSummary = filteredSummary.replace(idStr, name.lowercase())
                        }
                    }
                    
                    // 3. Fallback for any leftover unmapped IDs
                    filteredSummary = filteredSummary
                        .replace(Regex("(?i)item\\s+w_\\d+"), "this garment")
                        .replace(Regex("(?i)item\\s+c_\\d+"), "this product")
                        .replace(Regex("<ITEM:w_\\d+>"), "this garment")
                        .replace(Regex("<ITEM:c_\\d+>"), "this product")
                        .replace(Regex("w_\\d+"), "this garment")
                        .replace(Regex("c_\\d+"), "this product")
                        
                    Text(
                        text = filteredSummary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.DarkGray,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                analysis.advice.recommendedPalette.take(4).forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                            .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                    )
                }
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: Exception) {
        Color.Gray
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Curated Collection Card Preview")
@Composable
private fun CuratedCollectionCardPreview() {
    MaterialTheme {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(16.dp)) {
            val dummyAnalysis = com.zoewave.probase.core.model.ritual.SavedAnalysis(
                id = 1L,
                timestamp = System.currentTimeMillis(),
                advice = com.zoewave.probase.core.model.ritual.FashionAdvice(
                    title = "The Autumnal Neutral Capsule",
                    summary = "Combines warm earthy tones with structured silhouettes for daytime elegance.",
                    seasonalType = com.zoewave.probase.core.model.ritual.SeasonalType.AUTUMN,
                    undertone = com.zoewave.probase.core.model.ritual.Undertone.WARM,
                    makeupSuggestions = emptyList(),
                    outfitSuggestions = emptyList(),
                    recommendedPalette = listOf("#D4C4B7", "#2C2A29", "#7A8B76", "#C18C5D")
                )
            )
            CuratedCollectionCard(
                uiState = com.zoewave.probase.kocolor.mobile.features.home.ui.CuratedCollectionUiState(dummyAnalysis),
                onEvent = {},
                navTo = {}
            )
        }
    }
}
