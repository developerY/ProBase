package com.zoewave.probase.kocolor.features.store.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.store.R
import com.zoewave.probase.kocolor.features.store.ui.StoreEvent
import com.zoewave.probase.kocolor.features.store.ui.StoreUiState

private val AtelierBrown = Color(0xFF8C5A46)
private val AtelierDeepBrown = Color(0xFF6B4032)
private val AtelierBorder = Color(0xFFB99A8A)
private val AtelierWarmWash = Color(0xFFF7E9E0)
private val AtelierGlass = Color(0xFFFFF8F4)

@Composable
fun BioKoStore(
    uiState: StoreUiState,
    onEvent: (StoreEvent) -> Unit,
    navTo: (com.zoewave.probase.kocolor.model.KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(30.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.14f)
            )
            .clip(shape)
            .background(AtelierWarmWash)
            .clickable {
                onEvent(StoreEvent.EnterStore)
            }
    ) {

        // ---------------------------------------------------------
        // 1. Photography
        // ---------------------------------------------------------
        AsyncImage(
            model = R.drawable.applications_kocolor_features_store_kocolor_store_front,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            alpha = 0.90f
        )

        // ---------------------------------------------------------
        // 2. Warm editorial glass wash
        // ---------------------------------------------------------
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AtelierGlass.copy(alpha = 0.78f),
                            Color(0xFFF8EEE9).copy(alpha = 0.86f),
                            Color(0xFFF1DCD4).copy(alpha = 0.90f)
                        )
                    )
                )
        )

        // Subtle inner luminosity / vignette
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.34f),
                            Color.Transparent
                        ),
                        radius = 520f,
                        center = Offset.Unspecified
                    )
                )
        )

        // ---------------------------------------------------------
        // 3. Hairline border
        // ---------------------------------------------------------
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .clip(shape)
        ) {
            Surface(
                modifier = Modifier.matchParentSize(),
                color = Color.Transparent,
                shape = shape,
                border = BorderStroke(
                    width = 1.dp,
                    color = AtelierBorder.copy(alpha = 0.75f)
                )
            ) {}
        }

        // ---------------------------------------------------------
        // 4. Editorial content
        // ---------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 22.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ATELIER BOUTIQUE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.width(26.dp),
                    color = AtelierBrown.copy(alpha = 0.45f),
                    thickness = 1.dp
                )

                Text(
                    text = stringResource(
                        R.string.applications_kocolor_features_store_boutique_title
                    ).uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = AtelierBrown,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.4.sp,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(
                    modifier = Modifier.width(26.dp),
                    color = AtelierBrown.copy(alpha = 0.45f),
                    thickness = 1.dp
                )
            }

            // THE ART OF COLOR
            Text(
                text = stringResource(
                    R.string.applications_kocolor_features_store_boutique_subtitle
                ).uppercase(),
                modifier = Modifier.fillMaxWidth(),
                color = AtelierBrown,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                style = TextStyle(
                    fontSize = 29.sp,
                    lineHeight = 32.sp,
                    letterSpacing = 0.3.sp,
                    fontWeight = FontWeight.Normal,
                    shadow = Shadow(
                        color = Color.White.copy(alpha = 0.65f),
                        offset = Offset(1f, 1f),
                        blurRadius = 3f
                    )
                )
            )

            // Description
            Text(
                text = "Curated boutique — Science meets aesthetics",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                color = AtelierBrown.copy(alpha = 0.92f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Button
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(50.dp),
                color = Color.White.copy(alpha = 0.38f),
                border = BorderStroke(
                    width = 1.dp,
                    color = AtelierBorder.copy(alpha = 0.85f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 9.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "KoColor Store",
                        color = AtelierDeepBrown,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF6B3A2C),
                        modifier = Modifier.size(14.dp).rotate(-45f)
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFF8F4F1
)
@Composable
private fun BioStoreCardPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            BioKoStore(
                uiState = StoreUiState(),
                onEvent = {},
                navTo = {}
            )
        }
    }
}