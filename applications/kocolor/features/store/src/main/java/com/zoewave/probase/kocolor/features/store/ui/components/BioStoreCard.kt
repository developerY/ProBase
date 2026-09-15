package com.zoewave.probase.kocolor.features.store.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
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
                        colors = listOf(
                            Color(0xFFFFF7E6), // Warm Champagne
                            Color(0xFFFAF0F5), // Soft Pink Transition
                            Color(0xFFF7E6EC)  // Rose Blush
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Pill Badge (Centered)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, Color(0xFFEADBDF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF8B5A52)))
                        Text(
                            text = "CHROMATIC DNA: MUTED AUTUMN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5A52),
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // Subtitle Row with Divider Lines: — ATELIER BOUTIQUE — (Centered)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.width(28.dp),
                        color = Color(0xFF8B5A52).copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_store_boutique_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        color = Color(0xFF8B5A52),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.width(28.dp),
                        color = Color(0xFF8B5A52).copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                }

                // Main Display Title: The Art of Color (Centered)
                Text(
                    text = stringResource(R.string.applications_kocolor_features_store_boutique_subtitle),
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center
                )

                // Body Description (Centered)
                Text(
                    text = "Enter our curated boutique where science meets aesthetics. Tailored formulas and archival silhouettes matched to your chromatic harmony.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = Color(0xFF4A4A4A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(Modifier.height(4.dp))

                // Bottom Pill Button: [ ENTER ATELIER -> ] (Centered)
                Surface(
                    onClick = { 
                        android.util.Log.d("BioStoreCard", "ENTER ATELIER Clicked!")
                        onEvent(StoreEvent.EnterStore) 
                    }, 
                    color = Color(0xFF18101A), // Solid dark black-plum
                    shape = RoundedCornerShape(50),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ENTER ATELIER",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
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
