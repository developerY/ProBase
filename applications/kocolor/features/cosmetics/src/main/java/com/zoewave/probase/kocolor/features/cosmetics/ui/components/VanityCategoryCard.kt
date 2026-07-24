package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.CategoryMetadata
import com.zoewave.probase.kocolor.model.KoColorRoute

data class VanityCategoryUiState(
    val name: String,
    val metadata: CategoryMetadata?,
    val baseColor: Color,
    val fallbackImage: Any
)

@Composable
fun VanityCategoryCard(
    uiState: VanityCategoryUiState,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val name = uiState.name
    val metadata = uiState.metadata
    val baseColor = uiState.baseColor
    val fallbackImage = uiState.fallbackImage
    
    val count = metadata?.itemCount ?: 0
    val totalValue = metadata?.totalValue ?: 0.0
    val leadingBrand = metadata?.leadingBrand
    val averageFill = metadata?.averageFillLevel ?: 1.0
    val description = metadata?.description ?: when {
        name.contains("Skincare", ignoreCase = true) -> stringResource(R.string.applications_kocolor_features_cosmetics_desc_skincare)
        name.contains("Complexion", ignoreCase = true) -> stringResource(R.string.applications_kocolor_features_cosmetics_desc_complexion)
        name.contains("Dimension", ignoreCase = true) -> stringResource(R.string.applications_kocolor_features_cosmetics_desc_dimension)
        name.contains("Eyes", ignoreCase = true) -> stringResource(R.string.applications_kocolor_features_cosmetics_desc_eyes)
        name.contains("Lips", ignoreCase = true) -> stringResource(R.string.applications_kocolor_features_cosmetics_desc_lips)
        else -> stringResource(R.string.applications_kocolor_features_cosmetics_desc_default)
    }
    
    val currencyFormatter = remember { 
        java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US)
    }

    var showExplanation by remember { mutableStateOf(false) }

    if (showExplanation) {
        AlertDialog(
            onDismissRequest = { showExplanation = false },
            title = { Text(text = name, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = { Text(text = description) },
            confirmButton = {
                TextButton(onClick = { showExplanation = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }

    Card(
        onClick = { navTo(KoColorRoute.CosmeticCategoryCover(name)) },
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = metadata?.representativeImageUrl ?: fallbackImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.3f),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(baseColor, baseColor.copy(alpha = 0.9f), Color.Transparent),
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
                    Column(modifier = Modifier.weight(1f)) {
                        val displayName = if (name.contains("&")) name.substringBefore("&").trim() else name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp, lineHeight = 38.sp),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color.Black
                            )
                            IconButton(
                                onClick = { showExplanation = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Category Info",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Black.copy(alpha = 0.3f)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val itemsText = if (count == 1) stringResource(R.string.applications_kocolor_features_cosmetics_item_singular) else stringResource(R.string.applications_kocolor_features_cosmetics_items_plural_format, count)
                            Text(
                                text = itemsText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.alpha(0.9f)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = currencyFormatter.format(totalValue),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black
                        )
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_cosmetics_total_value),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.alpha(0.4f)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_cosmetics_stock_status), 
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp), 
                            fontWeight = FontWeight.Black, 
                            modifier = Modifier.alpha(0.6f)
                        )
                        Text(
                            text = "${(averageFill * 100).toInt()}%", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Black, 
                            color = Color(0xFF6B705C)
                        )
                    }
                    
                    val statusColor = Color(0xFF6B705C) 

                    LinearProgressIndicator(
                        progress = { averageFill.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = statusColor,
                        trackColor = Color.Black.copy(alpha = 0.05f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    if (leadingBrand != null) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_cosmetics_leading_brand_format, leadingBrand.uppercase()),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun VanityCategoryCardPreview() {
    MaterialTheme {
        VanityCategoryCard(
            uiState = VanityCategoryUiState(
                name = "Complexion",
                metadata = CategoryMetadata(itemCount = 12, totalValue = 540.0),
                baseColor = Color(0xFFF9F6F0),
                fallbackImage = R.drawable.vanity_complexion
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
