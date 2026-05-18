package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import java.text.NumberFormat
import java.util.Locale

private fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeDetailScreen(
    itemId: Long,
    uiState: WardrobeUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val item = remember(uiState.items, itemId) {
        uiState.items.find { it.id == itemId }
    } ?: return

    val itemColor = item.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Garment Details", style = MaterialTheme.typography.labelLarge) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onEvent(WardrobeEvent.UpdateDraft(item))
                        navTo(KoColorRoute.WardrobeEdit(item.id))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero
            Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(itemColor))
                if (item.imageUrl != null) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)))))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    Surface(color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp)) {
                        Text(text = item.category.name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(text = item.name, style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                }
            }

            // Metrics
            Row(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                MetricItem(label = "INVESTMENT", value = item.price?.let { currencyFormatter.format(it) } ?: "---", icon = Icons.Default.Payments, modifier = Modifier.weight(1f))
                MetricItem(label = "SIZE", value = item.size ?: "OS", icon = Icons.Default.Straighten, modifier = Modifier.weight(1f))
            }

            // Detail Content
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                Column {
                    SectionHeader("Composition")
                    DetailRow("Material", item.material ?: "Unknown")
                }
                Column {
                    SectionHeader("Archive Notes")
                    ProInsightCard(content = item.notes, icon = Icons.AutoMirrored.Filled.MenuBook)
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(text = title.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
}

@Composable
private fun MetricItem(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProInsightCard(content: String?, icon: ImageVector) {
    val isAvailable = !content.isNullOrBlank()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isAvailable) 0.5f else 0.1f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(text = content ?: "No notes provided.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isAvailable) 0.8f else 0.2f), lineHeight = 22.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WardrobeDetailScreenPreview() {
    WardrobeDetailScreen(
        itemId = 1L,
        uiState = WardrobeUiState(
            items = listOf(
                ClothingItem(id = 1L, name = "Silk Blouse", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS, price = 120.0)
            )
        ),
        onEvent = {},
        navTo = {}
    )
}
