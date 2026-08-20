package com.zoewave.probase.kocolor.features.starterpack.ui.synchub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import com.zoewave.probase.kocolor.features.starterpack.ui.SeedingState
import com.zoewave.probase.kocolor.features.starterpack.ui.StarterPackEvent
import com.zoewave.probase.kocolor.features.starterpack.ui.StarterPackUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncHubScreen(
    uiState: StarterPackUiState,
    filter: String? = null,
    onEvent: (StarterPackEvent) -> Unit,
    onNavigateTo: (KoColorRoute) -> Unit,
    onBack: () -> Unit
) {
    val serifFont = FontFamily.Serif
    var showWipeConfirmByPackId by remember { mutableStateOf<String?>(null) }
    var selectedInfoPack by remember { mutableStateOf<PackInfo?>(null) }

    // --- Filter logic based on the passed category, UI selector, and Search Query ---
    val filteredAvailablePacks = remember(uiState.availablePacks, filter, uiState.selectedCategory, uiState.searchQuery) {
        val baseFiltered = if (filter.isNullOrBlank()) {
            uiState.availablePacks
        } else {
            val fashionKeywords = listOf("fashion", "outerwear", "active", "dresses")
            if (filter.lowercase() == "clothing") {
                uiState.availablePacks.filter { pack -> 
                    fashionKeywords.any { pack.id.lowercase().contains(it) } 
                }
            } else if (filter.lowercase() == "cosmetics") {
                uiState.availablePacks.filter { pack -> 
                    !fashionKeywords.any { pack.id.lowercase().contains(it) } 
                }
            } else {
                uiState.availablePacks
            }
        }

        // Apply Sub-filter by UI Category Selector
        val categoryFiltered = if (uiState.selectedCategory == "ALL") {
            baseFiltered
        } else {
            val keywords = when (uiState.selectedCategory) {
                "LIPS" -> listOf("lips", "lipstick", "stain", "balm")
                "COMPLEXION" -> listOf("complexion", "foundation", "concealer", "powder", "spray", "bb-cc")
                "DIMENSION" -> listOf("dimension", "bronzer", "contour", "highlighter", "blush", "freckle")
                "EYES" -> listOf("eyes", "mascara", "eyeliner", "eyebrow", "lashes", "brow")
                "PREP" -> listOf("prep", "cleanser", "toner", "serum", "spf", "moisturizer", "exfoliant", "mask")
                "HAIR" -> listOf("hair", "shampoo", "conditioner", "scalp")
                "HYGIENE" -> listOf("hygiene", "soap", "wash", "deodorant", "antiperspirant", "cotton", "bath")
                "ORAL" -> listOf("oral", "toothpaste", "mouthwash", "toothbrush", "floss")
                "FRAGRANCE" -> listOf("frag", "perfume", "cologne", "mist", "aura")
                "TOOLS" -> listOf("tools", "brush", "sponge", "curler", "organizer", "spatula")
                "GROOMING" -> listOf("grooming", "razor", "aftershave", "beard")
                "NAILS" -> listOf("nails", "polish", "lacquer")
                // Apparel Keywords
                "TOPS" -> listOf("top", "shirt", "blouse", "knit", "tee", "camisole", "turtleneck")
                "BOTTOMS" -> listOf("bottom", "pants", "culottes", "leggings", "jeans", "slacks", "skirt")
                "DRESSES" -> listOf("dress", "jumpsuit", "dungarees", "slip")
                "OUTERWEAR" -> listOf("outerwear", "coat", "blazer", "jacket", "vest", "duster", "puffer")
                "ACTIVEWEAR" -> listOf("active", "hoodie", "leggings", "bra", "tank", "shorts")
                "SHOES" -> listOf("shoes", "boots", "flats", "heels", "sandals", "sneakers")
                else -> listOf(uiState.selectedCategory.lowercase())
            }
            
            baseFiltered.filter { pack ->
                pack.id == "com.kocolor.pack.cosmetics.complete" || 
                pack.id == "com.kocolor.pack.fashion.complete" || 
                keywords.any { kw -> 
                    pack.id.lowercase().contains(kw) || 
                    pack.name.lowercase().contains(kw) ||
                    pack.description.lowercase().contains(kw)
                }
            }
        }

        // Apply Search Filter (Final)
        if (uiState.searchQuery.isBlank()) {
            categoryFiltered
        } else {
            val q = uiState.searchQuery.lowercase()
            categoryFiltered.filter { pack ->
                pack.name.lowercase().contains(q) ||
                pack.description.lowercase().contains(q) ||
                pack.previewItems.any { it.name.lowercase().contains(q) || it.description.lowercase().contains(q) }
            }
        }
    }

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
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            Column {
                val hubTitle = if (filter?.lowercase() == "clothing") "Explore Fashion" else "Discover Cosmetics"
                GlowSyncTopAppBar(
                    title = hubTitle,
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(StarterPackEvent.SearchQueryChanged(it)) },
                    onBack = onBack
                )
                
                // Filter Chips
                val categories = if (filter?.lowercase() == "clothing") {
                    listOf("ALL", "TOPS", "BOTTOMS", "DRESSES", "OUTERWEAR", "ACTIVEWEAR", "SHOES")
                } else {
                    listOf("ALL", "LIPS", "COMPLEXION", "DIMENSION", "EYES", "PREP", "HAIR", "HYGIENE", "ORAL", "FRAGRANCE", "TOOLS", "GROOMING", "NAILS")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(Modifier.width(16.dp))
                    categories.forEach { cat ->
                        FilterChip(
                            selected = uiState.selectedCategory == cat,
                            onClick = { onEvent(StarterPackEvent.CategorySelected(cat)) },
                            label = { 
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (uiState.selectedCategory == cat) FontWeight.Bold else FontWeight.Medium
                                ) 
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF5A3854).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFF5A3854),
                                containerColor = Color.Transparent,
                                labelColor = Color.Gray
                            ),
                            border = null
                        )
                    }
                    Spacer(Modifier.width(16.dp))
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
            // Hero Section: Complete Collection
            val heroPackId = if (filter?.lowercase() == "clothing") "com.kocolor.pack.fashion.complete" else "com.kocolor.pack.cosmetics.complete"
            val heroPack = filteredAvailablePacks.find { it.id == heroPackId }
            
            if (heroPack != null) {
                item {
                    HeroPackageCard(
                        pack = heroPack,
                        status = uiState.installedPacks.find { it.packId == heroPack.id }?.status ?: PackStatus.AVAILABLE,
                        onImportClick = { 
                            onNavigateTo(KoColorRoute.PackPreview(
                                packId = heroPack.id, 
                                sha256 = heroPack.sha256, 
                                publisher = heroPack.publisher,
                                categoryFilter = filter
                            )) 
                        },
                        onInfoClick = { selectedInfoPack = heroPack }
                    )
                }
            }

            // Section Header
            item {
                Text(
                    text = if (filter?.lowercase() == "clothing") "The Fashion Catalog" else "The Cosmetics Catalog",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = serifFont,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Grid Catalog: Other Packs
            val otherPacks = filteredAvailablePacks.filter { it.id != heroPackId }
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
                            onImportClick = { 
                                onNavigateTo(KoColorRoute.PackPreview(
                                    packId = pack.id, 
                                    sha256 = pack.sha256, 
                                    publisher = pack.publisher,
                                    categoryFilter = filter
                                )) 
                            },
                            onInfoClick = { selectedInfoPack = pack },
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

    selectedInfoPack?.let { pack ->
        PackageInfoDialog(
            pack = pack,
            onDismiss = { selectedInfoPack = null }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncHubScreenPreview() {
    MaterialTheme {
        SyncHubScreen(
            uiState = StarterPackUiState(
                availablePacks = listOf(
                    PackInfo(
                        id = "com.kocolor.pack.core",
                        name = "KoColor Core Collection",
                        description = "The foundational high-fidelity product library.",
                        version = 1,
                        publisher = "KoColor",
                        packType = "STARTER_PACK",
                        endpoint = "core.json",
                        itemCount = 9,
                        compressedSizeBytes = 1024,
                        uncompressedSizeBytes = 2048,
                        sha256 = "hash",
                        signature = "sig",
                        compressionAlgorithm = "zstd",
                        hashAlgorithm = "sha256",
                        hashEncoding = "hex",
                        signatureAlgorithm = "ed25519",
                        signatureEncoding = "hex",
                        packageFormatVersion = 1,
                        schemaVersion = 1,
                        encryption = "none"
                    )
                )
            ),
            onEvent = {},
            onNavigateTo = {},
            onBack = {}
        )
    }
}
