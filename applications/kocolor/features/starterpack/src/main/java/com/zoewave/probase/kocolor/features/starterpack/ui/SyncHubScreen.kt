package com.zoewave.probase.kocolor.features.starterpack.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var searchActive by remember { mutableStateOf(false) }

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
            CenterAlignedTopAppBar(
                title = { Text("Glow Sync Hub", fontFamily = serifFont, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                DockedSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(StarterPackEvent.SearchQueryChanged(it)) },
                    onSearch = { searchActive = false },
                    active = searchActive,
                    onActiveChange = { searchActive = it },
                    placeholder = { Text("Search products or brands...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.filteredSearchIndex) { entry ->
                            ListItem(
                                headlineContent = { Text(entry.term) },
                                supportingContent = { Text(entry.brand) },
                                leadingContent = { 
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF745E7A)) 
                                },
                                modifier = Modifier.clickable {
                                    onNavigateTo(KoColorRoute.PackPreview(entry.packId, entry.id))
                                    searchActive = false
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFF745E7A).copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF745E7A),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Product Libraries",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = serifFont,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Power your AI styling with high-fidelity kits.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                items(uiState.availablePacks) { pack ->
                    val installed = uiState.installedPacks.find { it.packId == pack.id }
                    val status = installed?.status ?: PackStatus.AVAILABLE
                    val currentVersion = installed?.version ?: 0
                    val isUpdateAvailable = pack.version > currentVersion && status == PackStatus.INSTALLED

                    PackItemCard(
                        pack = pack,
                        status = status,
                        isUpdateAvailable = isUpdateAvailable,
                        onIngest = { onNavigateTo(KoColorRoute.PackPreview(packId = pack.id, sha256 = pack.sha256, publisher = pack.publisher)) },
                        onWipe = { showWipeConfirmByPackId = pack.id },
                        isLoading = uiState.seedingState is SeedingState.Loading
                    )
                }

                if (uiState.availablePacks.isEmpty() && !uiState.isRefreshing) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                            Text("No packs available.", color = Color.Gray)
                        }
                    }
                }
                
                item {
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun PackItemCard(
    pack: PackInfo,
    status: PackStatus,
    isUpdateAvailable: Boolean = false,
    onIngest: () -> Unit,
    onWipe: () -> Unit,
    isLoading: Boolean
) {
    val serifFont = FontFamily.Serif
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pack.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = serifFont,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pack.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                if (status == PackStatus.INSTALLED || isUpdateAvailable) {
                    Icon(
                        imageVector = if (isUpdateAvailable) Icons.Default.CloudDownload else Icons.Default.DownloadDone,
                        contentDescription = if (isUpdateAvailable) "Update Available" else "Installed",
                        tint = if (isUpdateAvailable) Color(0xFFFF9800) else Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${pack.itemCount} ITEMS • v${pack.version}",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (status == PackStatus.INSTALLED) {
                        IconButton(
                            onClick = onWipe,
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    Button(
                        onClick = onIngest,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status == PackStatus.INSTALLED || status == PackStatus.VERIFIED) Color.LightGray else Color(0xFF745E7A)
                        ),
                        enabled = !isLoading && status != PackStatus.DEPRECATED && status != PackStatus.REMOVED
                    ) {
                        val label = when (status) {
                            PackStatus.DOWNLOADING -> "SYNCING..."
                            PackStatus.VERIFIED -> "VERIFIED"
                            PackStatus.UPDATE_AVAILABLE -> "UPDATE"
                            PackStatus.INSTALLED -> "VIEW PACK"
                            PackStatus.DEPRECATED -> "DEPRECATED"
                            PackStatus.REMOVED -> "REMOVED"
                            else -> "PREVIEW"
                        }
                        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
