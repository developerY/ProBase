package com.zoewave.probase.kocolor.features.starterpack.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncHubScreen(
    uiState: StarterPackUiState,
    onEvent: (StarterPackEvent) -> Unit,
    onNavigateTo: (KoColorRoute) -> Unit,
    onBack: () -> Unit
) {
    val serifFont = FontFamily.Serif
    var showWipeConfirmByPackId by remember { mutableStateOf<String?>(null) }

    if (showWipeConfirmByPackId != null) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmByPackId = null },
            title = { Text("Wipe this Pack?", fontFamily = serifFont, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all items from this pack. Your personal scans will remain untouched.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showWipeConfirmByPackId?.let { onEvent(StarterPackEvent.OnWipePack(it)) }
                        showWipeConfirmByPackId = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("WIPE DATA", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmByPackId = null }) {
                    Text("CANCEL")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            Column {
                GlowSyncTopAppBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(StarterPackEvent.SearchQueryChanged(it)) },
                    onBack = onBack
                )
                // Filter Chips matching the mockup's implied categorization
                ScrollableTabRow(
                    selectedTabIndex = 0,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {}
                ) {
                    val categories = listOf("ALL", "LIPS", "COMPLEXION", "DIMENSION", "PREP", "COOL", "WARM")
                    categories.forEach { cat ->
                        FilterChip(
                            selected = cat == "ALL",
                            onClick = { /* Filter logic */ },
                            label = { Text(cat) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF5A3854).copy(alpha = 0.1f),
                                selectedLabelColor = Color(0xFF5A3854)
                            ),
                            border = null
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Section: Core Collection
            val heroPack = uiState.availablePacks.find { it.id == "com.kocolor.pack.core" }
            if (heroPack != null) {
                item {
                    HeroPackageCard(
                        pack = heroPack,
                        status = uiState.installedPacks.find { it.packId == heroPack.id }?.status ?: PackStatus.AVAILABLE,
                        onImportClick = { onNavigateTo(KoColorRoute.PackPreview(packId = heroPack.id, sha256 = heroPack.sha256, publisher = heroPack.publisher)) }
                    )
                }
            }

            // Section Header
            item {
                Text(
                    text = "Seasonal Collections",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = serifFont,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Grid Catalog: Other Packs
            val otherPacks = uiState.availablePacks.filter { it.id != "com.kocolor.pack.core" }
            items(otherPacks.chunked(2)) { rowPacks ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowPacks.forEach { pack ->
                        val installed = uiState.installedPacks.find { it.packId == pack.id }
                        CatalogPackageCard(
                            pack = pack,
                            status = installed?.status ?: PackStatus.AVAILABLE,
                            onImportClick = { onNavigateTo(KoColorRoute.PackPreview(packId = pack.id, sha256 = pack.sha256, publisher = pack.publisher)) },
                            onWipeClick = { showWipeConfirmByPackId = pack.id },
                            isLoading = uiState.seedingState is SeedingState.Loading,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowPacks.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlowSyncTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val serifFont = FontFamily.Serif
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
            "Glow Sync Hub",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = serifFont,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 16.dp)
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search products...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5)
            ),
            singleLine = true
        )
    }
}

@Composable
fun HeroPackageCard(
    pack: PackInfo,
    status: PackStatus,
    onImportClick: () -> Unit
) {
    val serifFont = FontFamily.Serif
    val plumColor = Color(0xFF5A3854)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image (Placeholder logic matching mockup)
            AsyncImage(
                model = pack.heroImageUrl ?: "https://cdn.kocolor.com/inventory/assets/hero_core.webp",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Overlay gradient or scrim could go here
            
            // Content Card
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(0.85f),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = pack.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = serifFont,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${pack.description} • ${pack.itemCount} Items",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onImportClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = plumColor),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (status == PackStatus.INSTALLED) "PREVIEW COLLECTION" else "DOWNLOAD COLLECTION",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            
            // Security Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                color = Color.White.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text("Verified Ed25519", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun CatalogPackageCard(
    pack: PackInfo,
    status: PackStatus,
    onImportClick: () -> Unit,
    onWipeClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val serifFont = FontFamily.Serif
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column {
            // Thumbnail
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
            ) {
                AsyncImage(
                    model = pack.heroImageUrl ?: "https://cdn.kocolor.com/inventory/assets/${pack.id}.webp",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                if (status == PackStatus.INSTALLED) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp).padding(2.dp), tint = Color(0xFF4CAF50))
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = serifFont,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = pack.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "• ${pack.itemCount} Items",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onImportClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5A3854)),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Text(if (status == PackStatus.INSTALLED) "PREVIEW" else "IMPORT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    if (status == PackStatus.INSTALLED) {
                        IconButton(
                            onClick = onWipeClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
