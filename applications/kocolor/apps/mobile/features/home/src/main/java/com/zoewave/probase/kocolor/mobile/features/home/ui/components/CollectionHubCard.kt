package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeUiState
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CollectionHubCard(
    uiState: HomeUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    val totalValue = uiState.totalVanityValue + uiState.totalWardrobeValue
    val totalItems = uiState.totalCosmetics + uiState.totalClothing

    ElevatedCard(
        modifier = modifier.fillMaxWidth().height(360.dp),
        shape = RoundedCornerShape(32.dp),
        onClick = { navTo(KoColorRoute.CollectionHub) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = R.drawable.collection_hub_background,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.25f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(32.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Collection\nHub",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 52.sp,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Unified Text, Color & Image\nSearch",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        lineHeight = 24.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "TOTAL VALUE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = currencyFormatter.format(totalValue),
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "$totalItems Items Tracked",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward, 
                                contentDescription = null, 
                                modifier = Modifier.size(28.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
