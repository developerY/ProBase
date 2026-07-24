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
import androidx.compose.ui.graphics.StrokeCap
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
        val (fullDesc, examples) = when {
            name.contains("Skincare", ignoreCase = true) -> 
                "The essential foundation for any routine. These products focus on cleaning, nourishing, and preparing the skin's surface before any color is applied." to 
                "Cleanser, Toner, Serum, Moisturizer, SPF, Primer, Face Mask, Exfoliant, Eye Care."
            name.contains("Complexion", ignoreCase = true) -> 
                "Architectural products designed to unify the skin tone, blur imperfections, and create a smooth, even canvas." to 
                "Foundation, BB/CC Cream, Concealer, Color Corrector, Setting Powder, Face Powder, Setting Spray."
            name.contains("Dimension", ignoreCase = true) -> 
                "Sculptural products that bring life, shadow, and light back to the face. These define your bone structure and add a natural-looking flush or glow." to 
                "Blush, Bronzer, Contour, Highlighter, Freckle Tint."
            name.contains("Eyes", ignoreCase = true) -> 
                "The focal point of visual communication. This category covers brow structure, lash enhancement, and artistic lid pigment." to 
                "Eyeshadow, Eyeliner, Mascara, Lash Primer, Brow Pencil, Brow Gel, False Lashes."
            name.contains("Lips", ignoreCase = true) -> 
                "Color and care for the mouth. Products range from hydrating treatments to high-impact pigments that define the final mood." to 
                "Lipstick, Lip Gloss, Lip Liner, Lip Tint/Stain, Lip Balm, Lip Plumper."
            name.contains("Nails", ignoreCase = true) -> 
                "Architectural enhancements for the hands. This includes color, protection, and structural care for the nails." to 
                "Nail Polish, Base Coat, Top Coat, Nail Treatment."
            else -> description to ""
        }

        AlertDialog(
            onDismissRequest = { showExplanation = false },
            title = { Text(text = name, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = fullDesc, 
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                    if (examples.isNotEmpty()) {
                        Column {
                            Text(
                                text = "INCLUDES:", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Black, 
                                color = Color.Black.copy(alpha = 0.4f),
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = examples, 
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.7f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            },
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
                        Brush.linearGradient(
                            colors = listOf(
                                baseColor.copy(alpha = 0.95f),
                                baseColor.copy(alpha = 0.8f),
                                Color.White.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp), // Slightly reduced padding to fit content
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
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 32.sp, 
                                    lineHeight = 36.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                fontFamily = FontFamily.Serif,
                                color = Color.Black
                            )
                            IconButton(
                                onClick = { showExplanation = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Category Info",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Black.copy(alpha = 0.2f)
                                )
                            }
                        }
                        Text(
                            text = if (count == 1) stringResource(R.string.applications_kocolor_features_cosmetics_item_singular) else stringResource(R.string.applications_kocolor_features_cosmetics_items_plural_format, count),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.alpha(0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = currencyFormatter.format(totalValue),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black
                        )
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_cosmetics_total_value),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.alpha(0.4f)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Glassy Stock Status Container
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.4f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.applications_kocolor_features_cosmetics_stock_status),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.Black.copy(alpha = 0.5f)
                            )
                            
                            LinearProgressIndicator(
                                progress = { averageFill.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = Color(0xFF7A6F5D), // Slightly darker for contrast
                                trackColor = Color.Black.copy(alpha = 0.05f),
                                strokeCap = StrokeCap.Round
                            )

                            Text(
                                text = "${(averageFill * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.4f)
                            )
                        }
                    }
                    
                    // Leading Brand
                    Text(
                        text = stringResource(
                            R.string.applications_kocolor_features_cosmetics_leading_brand_format, 
                            (leadingBrand ?: "Atelier").uppercase()
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp).alpha(0.7f),
                        color = Color.Black
                    )
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
