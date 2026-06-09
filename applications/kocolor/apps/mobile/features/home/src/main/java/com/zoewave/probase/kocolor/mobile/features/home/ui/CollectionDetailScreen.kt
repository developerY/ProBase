package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.MakeupSuggestion
import com.zoewave.probase.kocolor.model.OutfitSuggestion
import com.zoewave.probase.kocolor.model.SavedAnalysis
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    analysis: SavedAnalysis,
    navTo: (KoColorRoute) -> Unit
) {
    val advice = analysis.advice
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Atelier", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.Menu, null)
                    }
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            // Profile pic placeholder
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = advice.title ?: "The Silk Gala\nCollection",
                        style = MaterialTheme.typography.displayMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 44.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = advice.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 2. Hero Image
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    AsyncImage(
                        model = advice.clothesUri ?: R.drawable.advice_clothes_fallback,
                        contentDescription = "Hero Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 3. Color Story
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Color Story",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "FOUNDATIONAL TONES",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        modifier = Modifier.alpha(0.6f)
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        advice.recommendedPalette.take(4).forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(com.zoewave.probase.features.graphics.colorpicker.util.parseColor(hex))
                                    .border(1.dp, Color.Black.copy(alpha = 0.05f), CircleShape)
                            )
                        }
                    }
                }
            }

            // 4. The Wardrobe
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "The Wardrobe",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)
                }
            }

            advice.outfitSuggestions.forEach { outfit ->
                items(outfit.suggestedItems) { suggested ->
                    VerticalCollectionItem(
                        title = suggested.name,
                        description = suggested.description ?: "Professional selected piece for this look.",
                        imageModel = suggested.imageUrl ?: R.drawable.advice_clothes_fallback,
                        isOwned = suggested.isOwned
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // 5. The Vanity
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "The Vanity",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)
                }
            }

            items(advice.makeupSuggestions) { makeup ->
                VerticalCollectionItem(
                    title = makeup.suggestedProductName ?: makeup.category,
                    description = makeup.advice,
                    imageModel = makeup.suggestedProductImageUrl ?: R.drawable.advice_makeup_fallback,
                    isOwned = makeup.productId != null
                )
                Spacer(Modifier.height(16.dp))
            }
            
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

@Composable
private fun VerticalCollectionItem(
    title: String,
    description: String,
    imageModel: Any,
    isOwned: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isOwned) 1f else 0.4f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(80.dp),
            color = Color(0xFFF7F7F7)
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (!isOwned) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.width(20.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                if (!isOwned) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "SUGGESTED",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isOwned) Color.Gray else Color.Gray.copy(alpha = 0.6f),
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollectionDetailScreenPreview() {
    MaterialTheme {
        CollectionDetailScreen(
            analysis = SavedAnalysis(
                id = 1,
                timestamp = System.currentTimeMillis(),
                advice = FashionAdvice(
                    title = "The Silk Gala\nCollection",
                    summary = "A masterclass in textural contrast. Fluid champagne silk meets structured charcoal tailoring, designed for an evening of understated elegance.",
                    seasonalType = SeasonalType.WINTER,
                    undertone = Undertone.COOL,
                    makeupSuggestions = listOf(MakeupSuggestion("Lip", "Advice", listOf("#FF0000"))),
                    outfitSuggestions = listOf(OutfitSuggestion("Occasion", "Advice", listOf("Piece"), listOf("#000000"))),
                    recommendedPalette = listOf("#F3E5AB", "#8B4513", "#2C2C2C", "#EBC7B3")
                )
            ),
            navTo = {}
        )
    }
}
