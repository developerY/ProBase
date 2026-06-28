package com.zoewave.probase.kocolor.features.store.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
            .animateContentSize()
            .clickable { onEvent(StoreEvent.ToggleExpansion) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            // Background Image
            AsyncImage(
                model = uiState.backgroundModel ?: R.drawable.kocolor_store_front,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                alpha = 0.4f
            )
            
            // Premium Frosted Overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.75f))
            )

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.store_boutique_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.store_boutique_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                AnimatedVisibility(visible = uiState.isExpanded) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.store_boutique_description),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = Color(0xFF4A4A4A)
                        )
                        
                        Spacer(Modifier.height(32.dp))
                        
                        Surface(
                            onClick = { onEvent(StoreEvent.EnterStore) }, 
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.store_enter_atelier),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF8D6E63)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF8D6E63),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
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

@Preview(showBackground = true)
@Composable
private fun BioStoreCardExpandedPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BioStoreCard(
                uiState = StoreUiState(isExpanded = true),
                onEvent = {},
                navTo = {}
            )
        }
    }
}
