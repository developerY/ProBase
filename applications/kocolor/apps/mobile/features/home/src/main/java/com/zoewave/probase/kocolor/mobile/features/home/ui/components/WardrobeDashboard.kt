package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
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
private fun WardrobeDashboardPreview() {
    MaterialTheme {
        WardrobeDashboard(
            uiState = HomeUiState(totalClothing = 5, totalWardrobeValue = 1200.0),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun WardrobeDashboard(
    uiState: HomeUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val mostRecent = uiState.popularClothing.firstOrNull()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(32.dp),
        onClick = { navTo(KoColorRoute.WardrobeLanding) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Visual
            if (mostRecent?.imageUrl != null) {
                AsyncImage(
                    model = mostRecent.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.15f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFFE8F1FD).copy(alpha = 0.3f), Color.White)))
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
                            text = "THE STYLE ARCHIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${uiState.totalClothing} pieces curated",
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
                            Icon(Icons.Default.Checkroom, null, modifier = Modifier.size(24.dp), tint = Color.Black)
                        }
                    }
                }

                Column {
                    Text(
                        text = currencyFormatter.format(uiState.totalWardrobeValue),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "TOTAL CLOSET INVESTMENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(0.5f)
                    )
                }
            }
        }
    }
}
