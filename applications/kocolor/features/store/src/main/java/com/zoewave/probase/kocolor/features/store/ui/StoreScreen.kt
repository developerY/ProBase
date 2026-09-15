package com.zoewave.probase.kocolor.features.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import com.zoewave.probase.kocolor.features.store.R
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    uiState: StoreUiState,
    modifier: Modifier = Modifier,
    onEvent: (StoreEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.applications_kocolor_features_store_title),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_store_back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFFDFDFD)
    ) { padding ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            
            // Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFEBE3E8))
                        .clickable { navTo(KoColorRoute.StarterPack(filter = "cosmetics")) }
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_store_boutique_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 2.sp,
                            color = Color(0xFF8B5A52),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_store_boutique_title),
                            style = MaterialTheme.typography.displaySmall,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Enter our curated boutique where science meets aesthetics.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        Spacer(Modifier.height(24.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF1A1A1A)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(stringResource(R.string.applications_kocolor_features_store_enter_atelier), style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            
            // Cosmetics Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_store_cosmetics_vault),
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_store_see_all),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF8B5A52),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { navTo(KoColorRoute.StarterPack(filter = "cosmetics")) }
                        )
                    }
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.cosmeticsPacks) { pack ->
                            StorePackCard(
                                pack = pack,
                                onClick = { navTo(KoColorRoute.PackPreview(packId = pack.id, sha256 = pack.sha256, publisher = pack.publisher)) }
                            )
                        }
                    }
                }
            }
            
            // Wardrobe Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_store_fashion_archive),
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_store_see_all),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF8B5A52),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { navTo(KoColorRoute.StarterPack(filter = "clothing")) }
                        )
                    }
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.fashionPacks) { pack ->
                            StorePackCard(
                                pack = pack,
                                onClick = { navTo(KoColorRoute.PackPreview(packId = pack.id, sha256 = pack.sha256, publisher = pack.publisher)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorePackCard(
    pack: PackInfo,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Color(0xFFF5F5F5))) {
                // Background Image
                if (pack.heroImageUrl != null) {
                    AsyncImage(
                        model = pack.heroImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.applications_kocolor_features_store_items_curated, pack.itemCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
