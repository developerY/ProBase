package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeUiState
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale

@Preview(showBackground = true)
@Composable
private fun InventoryDashboardPreview() {
    MaterialTheme {
        InventoryDashboard(
            uiState = HomeUiState(totalCosmetics = 10, totalVanityValue = 500.0),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun InventoryDashboard(
    uiState: HomeUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val mostPopular = uiState.popularCosmetics.firstOrNull()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(32.dp),
        onClick = { navTo(KoColorRoute.VanityLanding) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Visual
            if (mostPopular?.imageUrl != null) {
                AsyncImage(
                    model = mostPopular.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.15f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFFFDEEF4).copy(alpha = 0.3f), Color.White)))
                )
            }

            // High-Density Content
            Column(
                modifier = Modifier.padding(28.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "THE VANITY VAULT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${uiState.totalCosmetics} curated items",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.8f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Face, null, modifier = Modifier.size(24.dp), tint = Color.Black)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = currencyFormatter.format(uiState.totalVanityValue),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "NET COLLECTION VALUE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(0.5f)
                        )
                    }
                    
                    if (uiState.expiringCosmeticsCount > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(6.6.dp))
                                Text(
                                    text = "${uiState.expiringCosmeticsCount} EXPIRING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
