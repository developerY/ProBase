package com.zoewave.probase.kocolor.features.starterpack.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarterPackScreen(
    uiState: StarterPackUiState,
    onIngest: (PackInfo) -> Unit,
    onWipe: (String) -> Unit,
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
                        showWipeConfirmByPackId?.let { onWipe(it) }
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
                title = { Text("Glow Archive Sync", fontFamily = serifFont, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFF745E7A).copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF745E7A),
                            modifier = Modifier.size(56.dp)
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

                PackItemCard(
                    pack = pack,
                    status = status,
                    onIngest = { onIngest(pack) },
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

@Composable
fun PackItemCard(
    pack: PackInfo,
    status: PackStatus,
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
                
                if (status == PackStatus.INSTALLED) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = "Installed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pack.item_count} ITEMS • v${pack.version}",
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
                            containerColor = if (status == PackStatus.INSTALLED) Color.LightGray else Color(0xFF745E7A)
                        ),
                        enabled = !isLoading
                    ) {
                        val label = when (status) {
                            PackStatus.AVAILABLE -> "DOWNLOAD"
                            PackStatus.DOWNLOADING -> "SYNCING..."
                            PackStatus.INSTALLED -> "UPDATE"
                        }
                        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
