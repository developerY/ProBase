package com.zoewave.probase.kocolor.features.store.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.store.R
import com.zoewave.probase.kocolor.features.store.ui.StoreEvent
import com.zoewave.probase.kocolor.features.store.ui.StoreUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun BioStoreCard(
    uiState: StoreUiState,
    onEvent: (StoreEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { 
                android.util.Log.d("BioStoreCard", "BioStoreCard Clicked -> Navigating to Store")
                onEvent(StoreEvent.EnterStore) 
            },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFCF3F5), Color(0xFFF7E6EC))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Pill Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFE8D3D8).copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF8B5A52)))
                        Text(
                            text = "CHROMATIC DNA: MUTED AUTUMN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B3A32),
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_store_boutique_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_store_boutique_title),
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }

                Text(
                    text = "Enter our curated boutique where science meets aesthetics. Tailored formulas and archival silhouettes matched to your chromatic harmony.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = Color(0xFF4A4A4A)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXPLORE ATELIER",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF8B5A52)
                    )

                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Enter Store",
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BioStoreCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BioStoreCard(
                uiState = StoreUiState(),
                onEvent = {},
                navTo = {}
            )
        }
    }
}
