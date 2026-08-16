package com.zoewave.probase.kocolor.features.inventory.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.ui.util.rememberBlurHashPainter
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.CategoryMetadata
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.*

data class SummaryStatUiState(
    val label: String,
    val value: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryStatCard(
    uiState: SummaryStatUiState,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val isValue = uiState.label.contains("VALUE")
    val charcoal = Color(0xFF2C2420)
    
    val valueBrush = if (isValue) {
        Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF4CAF50), Color(0xFF1B5E20)))
    } else {
        null
    }

    val actionBg = if (isValue) {
        Brush.linearGradient(listOf(Color(0xFF003300), Color(0xFF006600), Color(0xFF003300)))
    } else {
        Brush.linearGradient(listOf(
            Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFADAD), 
            Color(0xFFFFD6A5), Color(0xFFFDFFB6), Color(0xFFCAFFBF)
        ))
    }
    
    val actionText = if (isValue) "VIEW INVENTORY" else "VIEW INTELLIGENCE"
    val actionContentColor = if (isValue) Color.White else charcoal.copy(alpha = 0.8f)

    val glassBg = if (isValue) {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFF1F8F1)))
    } else {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFF8F9FF)))
    }

    Card(
        modifier = modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        onClick = onEvent,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().background(glassBg)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = uiState.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = charcoal.copy(alpha = 0.4f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (valueBrush != null) {
                    Text(
                        text = uiState.value,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = if (uiState.value.length > 6) 38.sp else 64.sp,
                            fontFamily = FontFamily.Serif,
                            brush = valueBrush
                        ),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = uiState.value,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        fontWeight = FontWeight.Bold,
                        color = charcoal
                    )
                }
                
                Text(
                    text = uiState.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    ),
                    color = charcoal.copy(alpha = 0.5f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(actionBg)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.White.copy(alpha = if (isValue) 0.12f else 0.45f), 
                    border = BorderStroke(0.5.dp, actionContentColor.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Black,
                            color = actionContentColor,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = actionContentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WardrobeTaxonomyDialog(
    uiState: Unit,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    AlertDialog(
        onDismissRequest = onEvent,
        title = { Text(stringResource(R.string.applications_kocolor_features_inventory_architecture_title), style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                item {
                    TaxonomySection(
                        uiState = TaxonomySectionUiState(
                            level = "Level 1",
                            title = stringResource(R.string.applications_kocolor_features_inventory_verticals_title),
                            description = stringResource(R.string.applications_kocolor_features_inventory_verticals_desc),
                            items = listOf("Tops" to "Blazers, Shirts, Knitwear.", "Bottoms" to "Trousers, Skirts, Denim.", "Shoes" to "Heels, Flats, Sneakers.", "Accessories" to "Bags, Belts, Jewelry.")
                        ),
                        onEvent = {},
                        navTo = {}
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onEvent) { Text("Understand", fontWeight = FontWeight.Bold) } },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFF9F6F0)
    )
}

data class TaxonomySectionUiState(
    val level: String,
    val title: String,
    val description: String,
    val items: List<Pair<String, String>>
)

@Composable
private fun TaxonomySection(
    uiState: TaxonomySectionUiState,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(text = uiState.level.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(text = uiState.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = uiState.description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.7f))
        }
        Surface(color = Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.items.forEach { (label, detail) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "•", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(text = detail, style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.7f))
                        }
                    }
                }
            }
        }
    }
}

data class AtelierWardrobeUiState(
    val name: String,
    val metadata: CategoryMetadata?,
    val baseColor: Color,
    val imageModel: Any
)

@Composable
fun AtelierWardrobeCard(
    uiState: AtelierWardrobeUiState,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val name = uiState.name
    val metadata = uiState.metadata
    val baseColor = uiState.baseColor
    val imageModel = uiState.imageModel
    
    val count = metadata?.itemCount ?: 0
    val totalValue = metadata?.totalValue ?: 0.0
    val leadingBrand = metadata?.leadingBrand
    val averageUsage = metadata?.averageUsage ?: 0.0
    val description = metadata?.description ?: "Strategic curated wardrobe collection."
    
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        onClick = { navTo(KoColorRoute.WardrobeCategoryCover(categoryName = name)) },
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val placeholder = rememberBlurHashPainter(
                blurHash = null, // Backend can be updated later to provide category blurhashes
                fallbackColor = baseColor
            )
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                placeholder = placeholder,
                modifier = Modifier.fillMaxSize().alpha(0.4f),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(baseColor, baseColor.copy(alpha = 0.6f), Color.Transparent),
                            startX = 0f,
                            endX = 1000f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.applications_kocolor_features_inventory_pieces_format, count),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.alpha(0.8f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "|  $description",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.alpha(0.6f)
                            )
                        }
                    }

                    Text(
                        text = currencyFormatter.format(totalValue),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        color = Color.Black
                    )
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.6f).padding(top = 2.dp),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = stringResource(R.string.applications_kocolor_features_inventory_average_utility), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.alpha(0.8f))
                        Text(text = stringResource(R.string.applications_kocolor_features_inventory_wears_format_plural, averageUsage.toInt()), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF6B705C))
                    }
                    
                    val maxWears = 50.0 
                    val progress = (averageUsage / maxWears).coerceIn(0.0, 1.0)
                    val statusColor = Color(0xFF6B705C) 

                    LinearProgressIndicator(
                        progress = { progress.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = statusColor,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    if (leadingBrand != null) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_inventory_leading_brand_label_format, leadingBrand),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentClothingCard(uiState: ClothingItem, modifier: Modifier = Modifier, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    val item = uiState
    val onClick = { navTo(KoColorRoute.WardrobeDetail(item.internalId)) }
    Card(
        modifier = modifier.width(220.dp).aspectRatio(0.8f).clickable { onClick() },
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) {
                val placeholder = rememberBlurHashPainter(blurHash = item.blurhash)
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    placeholder = placeholder,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                val itemColor = item.dominantHex?.let { parseColor(it) } 
                    ?: item.colorHex?.let { parseColor(it) } 
                    ?: Color.White
                
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(24.dp)
                        .align(Alignment.TopEnd),
                    color = itemColor,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {}
            } else {
                val itemColor = item.dominantHex?.let { parseColor(it) } 
                    ?: item.colorHex?.let { parseColor(it) } 
                    ?: MaterialTheme.colorScheme.surfaceVariant
                Box(modifier = Modifier.fillMaxSize().background(itemColor))
            }
            
            Surface(
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = item.category.name.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.Black
                )
            }

            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)), startY = 300f)))
            
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = item.brand ?: "", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}
