package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.ui.util.PremiumProductImage
import com.zoewave.probase.core.ui.util.isColorDark
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.components.DetailRow
import com.zoewave.probase.kocolor.features.inventory.ui.components.MetricItem
import com.zoewave.probase.kocolor.features.inventory.ui.components.ProInsightCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.SectionHeader
import com.zoewave.probase.core.model.ritual.ArchiveStatus
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.InventorySource
import com.zoewave.probase.core.ui.components.MakeItMineButton
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.*

data class WardrobeDetailUiState(
    val itemId: Long,
    val wardrobeUiState: WardrobeUiState,
    val archiveStatus: ArchiveStatus = ArchiveStatus.IDLE
)

@Preview(showBackground = true)
@Composable
private fun WardrobeDetailScreenPreview() {
    MaterialTheme {
        WardrobeDetailScreen(
            uiState = WardrobeDetailUiState(
                itemId = 1,
                wardrobeUiState = WardrobeUiState(
                    items = listOf(
                        ClothingItem(
                            internalId = 1, 
                            name = "Silk Blouse", 
                            brand = "Celine", 
                            category = ClothingCategory.TOPS, 
                            price = 850.0, 
                            usageCount = 12, 
                            colorHex = "#F5F5F5",
                            material = "100% Silk"
                        )
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeDetailScreen(
    uiState: WardrobeDetailUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val item = uiState.wardrobeUiState.items.find { it.internalId == uiState.itemId } ?: return
    val atelierBrown = Color(0xFF8B5E3C)
    val archiveStatus = uiState.archiveStatus

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_inventory_garment_details), style = MaterialTheme.typography.labelLarge) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_inventory_back))
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.WardrobeEdit(item.internalId)) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.applications_kocolor_features_inventory_edit))
                    }
                }
            )
        },
        bottomBar = {
            if (item.sourceType != InventorySource.USER_SCAN && item.sourceType != InventorySource.CLONED) {
                Box(modifier = Modifier.padding(16.dp)) {
                    MakeItMineButton(
                        status = archiveStatus,
                        onClick = { onEvent(WardrobeEvent.CloneToPersonal(item)) },
                        containerColor = atelierBrown
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f),
                color = Color(0xFFFBF8F5)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (item.imageUrl != null) {
                        PremiumProductImage(
                            imageUrl = item.imageUrl,
                            blurHash = item.blurhash,
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            fallbackColor = parseColor(item.colorHex ?: "#FFFFFF")
                        )
                    }
                    
                    // Category Badge
                    Surface(
                        modifier = Modifier.padding(24.dp).align(Alignment.TopStart),
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = item.category.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // 2. Data Content
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.brand ?: stringResource(R.string.applications_kocolor_features_inventory_brand_default),
                            style = MaterialTheme.typography.labelLarge,
                            color = atelierBrown,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Dominant Color Dot
                    val dominantColor = item.dominantHex?.let { parseColor(it) } ?: item.colorHex?.let { parseColor(it) } ?: Color.Transparent
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = dominantColor,
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
                    ) {}
                }

                Spacer(Modifier.height(32.dp))

                // Performance Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                    MetricItem(label = stringResource(R.string.applications_kocolor_features_inventory_investment), value = currencyFormatter.format(item.price ?: 0.0), modifier = Modifier.weight(1f))
                    MetricItem(label = stringResource(R.string.applications_kocolor_features_inventory_cost_per_wear), value = item.costPerUse?.let { currencyFormatter.format(it) } ?: stringResource(R.string.applications_kocolor_features_inventory_not_available), modifier = Modifier.weight(1f))
                    MetricItem(label = stringResource(R.string.applications_kocolor_features_inventory_wears), value = item.usageCount.toString(), modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { onEvent(WardrobeEvent.WearItem(item.internalId)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = atelierBrown)
                ) {
                    Icon(Icons.Default.Checkroom, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.applications_kocolor_features_inventory_log_wear), fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(40.dp))

                SectionHeader(stringResource(R.string.applications_kocolor_features_inventory_composition))
                DetailRow(label = stringResource(R.string.applications_kocolor_features_inventory_material), value = item.material ?: stringResource(R.string.applications_kocolor_features_inventory_unknown))
                
                Spacer(Modifier.height(24.dp))
                
                SectionHeader(stringResource(R.string.applications_kocolor_features_inventory_archive_notes))
                Text(
                    text = item.notes ?: stringResource(R.string.applications_kocolor_features_inventory_no_notes),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(32.dp))
                
                ProInsightCard(text = "Strategic curated wardrobe collection.")
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
