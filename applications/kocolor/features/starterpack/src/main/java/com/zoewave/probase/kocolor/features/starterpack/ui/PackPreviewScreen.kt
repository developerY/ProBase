package com.zoewave.probase.kocolor.features.starterpack.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItem
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackPreviewScreen(
    uiState: PackPreviewUiState,
    onToggleSelection: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onImportSelected: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val serifFont = FontFamily.Serif

    LaunchedEffect(uiState.targetItemId) {
        val targetId = uiState.targetItemId
        if (targetId != null) {
            val index = uiState.items.indexOfFirst { it.id == targetId }
            if (index != -1) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Items", fontFamily = serifFont, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onSelectAll) {
                        Text("Select All")
                    }
                    TextButton(onClick = onDeselectAll) {
                        Text("Clear")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onImportSelected,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedIds.isNotEmpty() && !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF745E7A))
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Import Selected (${uiState.selectedIds.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                itemsIndexed(uiState.items) { index, item ->
                    PackItemRow(
                        item = item,
                        isSelected = uiState.selectedIds.contains(item.id),
                        isTarget = item.id == uiState.targetItemId,
                        onToggle = { onToggleSelection(item.id) }
                    )
                    if (index < uiState.items.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    }
                }
                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun PackItemRow(
    item: PackItem,
    isSelected: Boolean,
    isTarget: Boolean,
    onToggle: () -> Unit
) {
    var highlightActive by remember { mutableStateOf(isTarget) }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (highlightActive) Color(0xFF745E7A).copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 1000),
        label = "highlight"
    )

    LaunchedEffect(isTarget) {
        if (isTarget) {
            highlightActive = true
            delay(2000)
            highlightActive = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${item.brand} • ${item.shade}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(item.hexColor))
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = item.hexColor,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
        }

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: Exception) {
        Color.Gray
    }
}
