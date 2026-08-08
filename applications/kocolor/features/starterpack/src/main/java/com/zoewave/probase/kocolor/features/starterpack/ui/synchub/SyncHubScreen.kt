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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
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
    onEvent: (StarterPackEvent) -> Unit,
    onNavigateTo: (KoColorRoute) -> Unit,
    onBack: () -> Unit
) {
    val serifFont = FontFamily.Serif
    var showWipeConfirmByPackId by remember { mutableStateOf<String?>(null) }
    var selectedInfoPack by remember { mutableStateOf<PackInfo?>(null) }

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
                GlowSyncTopAppBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(StarterPackEvent.SearchQueryChanged(it)) },
                    onBack = onBack
                )
                // Filter Chips
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
                        onImportClick = { onNavigateTo(KoColorRoute.PackPreview(packId = heroPack.id, sha256 = heroPack.sha256, publisher = heroPack.publisher)) },
                        onInfoClick = { selectedInfoPack = heroPack }
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
                        name = "Core Collection",
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
