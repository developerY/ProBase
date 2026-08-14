package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.ProfessionalTaxonomyDialog
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.RecentProductCard
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.SummaryStatCard
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.SummaryStatUiState
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.VanityCategoryCard
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.VanityCategoryUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun VanityLandingScreenPreview() {
    MaterialTheme {
        VanityLandingScreen(
            uiState = CosmeticsUiState(
                totalCosmetics = 34,
                items = listOf(CosmeticItem(name = "Sample", brand = "Brand", macroCategory = MacroCategory.COMPLEXION, microCategory = MicroCategory.FOUNDATION, colorHex = "#FFFFFF"))
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VanityLandingScreen(
    uiState: CosmeticsUiState,
    modifier: Modifier = Modifier,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showTaxonomyInfo by remember { mutableStateOf(false) }

    // --- Shimmer Animation Logic ---
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    if (showTaxonomyInfo) {
        ProfessionalTaxonomyDialog(
            onEvent = { showTaxonomyInfo = false },
            navTo = {}
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_glow_archive), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_back))
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.DiscoveryStatus) }) {
                        Icon(Icons.Default.CloudDone, contentDescription = "Discovery Health")
                    }
                    IconButton(onClick = { navTo(KoColorRoute.StarterPack) }) { 
                        Icon(Icons.Default.Sync, contentDescription = "Glow Sync") 
                    }
                    IconButton(onClick = { navTo(KoColorRoute.BoxCapture()) }) { 
                        Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_scan_box_title)) 
                    }
                    IconButton(onClick = { navTo(KoColorRoute.InventoryManagement) }) { Icon(Icons.Default.Inventory2, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_inventory_title)) }
                    IconButton(onClick = { navTo(KoColorRoute.ColorSearch) }) { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_filter)) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navTo(KoColorRoute.CosmeticAdd()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_add_item))
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_cosmetics_welcome_header),
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_cosmetics_welcome_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // --- PROMINENT COLOR HUB ENTRY ---
            item {
                val chromaticBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFADAD),
                        Color(0xFFFFD6A5), Color(0xFFFDFFB6), Color(0xFFCAFFBF)
                    )
                )

                Surface(
                    onClick = { navTo(KoColorRoute.ColorHub) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                ) {
                    val shimmerBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.3f),
                            Color(0xFFA0C4FF).copy(alpha = 0.15f), // Subtle blue tint
                            Color.White.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        start = Offset(x = shimmerProgress * 1000f, y = 0f),
                        end = Offset(x = (shimmerProgress + 0.3f) * 1000f, y = 500f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(shimmerBrush)
                            .padding(horizontal = 20.dp)
                    ) {
                        // Icon with Chromatic background circle
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(chromaticBrush, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(Modifier.width(20.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Color Intelligence Hub",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF2C2420)
                            )
                            Text(
                                text = "Spectral blueprint & chromatic DNA",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.LightGray.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // --- PROMINENT GLOW SYNC HUB ENTRY ---
            item {
                Surface(
                    onClick = { navTo(KoColorRoute.StarterPack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF5A3854), // Elegant Plum
                    shadowElevation = 8.dp
                ) {
                    val shimmerBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.1f),
                            Color(0xFFD4AF37).copy(alpha = 0.15f), // Gold tint
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        start = Offset(x = shimmerProgress * 1000f, y = 0f),
                        end = Offset(x = (shimmerProgress + 0.3f) * 1000f, y = 500f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(shimmerBrush)
                            .padding(horizontal = 20.dp)
                    ) {
                        // Icon with Gold background circle
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFD4AF37), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(Modifier.width(20.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Glow Sync Hub",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color.White
                            )
                            Text(
                                text = "High-fidelity scientific collections",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryStatCard(
                        uiState = SummaryStatUiState(
                            label = stringResource(R.string.applications_kocolor_features_cosmetics_total_products),
                            value = uiState.totalCosmetics.toString(),
                            icon = Icons.Default.Inventory2
                        ),
                        modifier = Modifier.weight(1f),
                        onEvent = { navTo(KoColorRoute.InventoryManagement) }
                    )
                    SummaryStatCard(
                        uiState = SummaryStatUiState(
                            label = stringResource(R.string.applications_kocolor_features_cosmetics_expiring_soon),
                            value = uiState.expiringCosmeticsCount.toString(),
                            icon = Icons.Default.ErrorOutline
                        ),
                        modifier = Modifier.weight(1f),
                        onEvent = { navTo(KoColorRoute.ExpiringSoon) }
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_cosmetics_categories),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            onClick = { showTaxonomyInfo = true },
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFD4AF37)), 
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.applications_kocolor_features_cosmetics_info_icon),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF2C2420)
                                )
                            }
                        }
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val sections = listOf(
                            "Skincare & Prep" to (Color(0xFFF7F2EB) to R.drawable.vanity_skincare),
                            "Complexion" to (Color(0xFFF9F6F0) to R.drawable.vanity_complexion),
                            "Color & Dimension" to (Color(0xFFFDEEF4) to R.drawable.vanity_color),
                            "Eyes & Brows" to (Color(0xFFE8F1FD) to R.drawable.vanity_eyes),
                            "Lips" to (Color(0xFFFEECEB) to R.drawable.vanity_lips)
                        )
                        
                        sections.forEach { (name, props) ->
                            val (bgColor, fallbackImage) = props
                            val metadata = uiState.categoriesMetadata.entries.find { it.key.contains(name, ignoreCase = true) }?.value
                            VanityCategoryCard(
                                uiState = VanityCategoryUiState(
                                    name = name,
                                    metadata = metadata,
                                    baseColor = bgColor,
                                    fallbackImage = fallbackImage
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                onEvent = {},
                                navTo = navTo
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_recently_added), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(
                            stringResource(R.string.applications_kocolor_features_cosmetics_see_all),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { navTo(KoColorRoute.Cosmetics()) }
                        )
                    }
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(uiState.items.take(5)) { item ->
                            RecentProductCard(
                                uiState = item,
                                modifier = Modifier,
                                onEvent = {},
                                navTo = navTo
                            )
                        }
                    }
                }
            }
        }
    }
}
