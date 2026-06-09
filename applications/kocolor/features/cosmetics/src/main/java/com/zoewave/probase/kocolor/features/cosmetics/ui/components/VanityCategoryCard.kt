package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

@Composable
fun VanityCategoryCard(
    name: String,
    metadata: CategoryMetadata?,
    baseColor: Color,
    fallbackImage: Any,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val count = metadata?.itemCount ?: 0
    val totalValue = metadata?.totalValue ?: 0.0
    val leadingBrand = metadata?.leadingBrand
    val averageFill = metadata?.averageFillLevel ?: 1.0
    val description = metadata?.description ?: when {
        name.contains("Skincare", ignoreCase = true) -> "Everything applied before pigment."
        name.contains("Complexion", ignoreCase = true) -> "Products that unify the skin tone."
        name.contains("Dimension", ignoreCase = true) -> "Products that bring life, shadow, and light."
        name.contains("Eyes", ignoreCase = true) -> "All definition for the upper face."
        name.contains("Lips", ignoreCase = true) -> "All color and care for the mouth."
        else -> "Professional curated category."
    }
    
    val currencyFormatter = remember { 
        java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US)
    }

    Card(
        onClick = { navTo(KoColorRoute.CosmeticCategoryCover(name)) },
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Imagery
            AsyncImage(
                model = metadata?.representativeImageUrl ?: fallbackImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.3f),
                contentScale = ContentScale.Crop
            )
            
            // 2. Readability Scrim
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

            // 3. Data Content
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
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp, lineHeight = 38.sp),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val itemsText = if (count == 1) stringResource(R.string.applications_kocolor_features_cosmetics_item_singular) else stringResource(R.string.applications_kocolor_features_cosmetics_items_plural_format, count)
                            Text(
                                text = itemsText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.alpha(0.9f)
                            )
                            Text(
                                text = "  |  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.3f)
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.alpha(0.6f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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

                // Stock Status Bar
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
            name = "Complexion",
            metadata = CategoryMetadata(itemCount = 12, totalValue = 540.0),
            baseColor = Color(0xFFF9F6F0),
            fallbackImage = R.drawable.vanity_complexion,
            navTo = {}
        )
    }
}
