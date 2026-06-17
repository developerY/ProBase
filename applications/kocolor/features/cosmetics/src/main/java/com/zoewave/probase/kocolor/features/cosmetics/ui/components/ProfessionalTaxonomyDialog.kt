package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.model.KoColorRoute

data class ProfessionalTaxonomyUiState(
    val modifier: Modifier = Modifier
)

@Composable
fun ProfessionalTaxonomyDialog(
    uiState: ProfessionalTaxonomyUiState,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    AlertDialog(
        onDismissRequest = onEvent,
        title = { 
            Text(
                stringResource(R.string.applications_kocolor_features_cosmetics_taxonomy_title), 
                style = MaterialTheme.typography.headlineMedium, 
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    TaxonomySection(
                        uiState = TaxonomySectionUiState(
                            level = stringResource(R.string.applications_kocolor_features_cosmetics_taxonomy_level_format, "1"),
                            title = stringResource(R.string.applications_kocolor_features_cosmetics_taxonomy_macro_title),
                            description = stringResource(R.string.applications_kocolor_features_cosmetics_taxonomy_macro_desc),
                            items = listOf(
                                "Skincare & Prep" to "Applied before pigment.",
                                "Complexion (Base)" to "Unifies skin tone.",
                                "Color & Dimension" to "Life, shadow, and light.",
                                "Eyes & Brows" to "Upper face definition.",
                                "Lips" to "Color and care.",
                                "Tools & Hygiene" to "Application and sanitization."
                            )
                        ),
                        onEvent = {},
                        navTo = {}
                    )
                }
                
                item {
                    TaxonomySection(
                        uiState = TaxonomySectionUiState(
                            level = stringResource(R.string.applications_kocolor_features_cosmetics_taxonomy_level_format, "2"),
                            title = stringResource(R.string.applications_kocolor_features_cosmetics_taxonomy_micro_title),
                            description = stringResource(R.string.applications_kocolor_features_cosmetics_taxonomy_micro_desc),
                            items = listOf(
                                "Skincare" to "Cleanser, Toner, Serum, SPF, Primer.",
                                "Complexion" to "Foundation, Concealer, Setting Powder.",
                                "Dimension" to "Blush, Bronzer, Contour, Highlighter.",
                                "Eyes" to "Eyeshadow, Eyeliner, Mascara, Brow Gel.",
                                "Lips" to "Lipstick, Gloss, Liner, Stain, Balm."
                            )
                        ),
                        onEvent = {},
                        navTo = {}
                    )
                }

                item {
                    TaxonomySection(
                        uiState = TaxonomySectionUiState(
                            level = stringResource(R.string.applications_kocolor_features_cosmetics_taxonomy_level_format, "3"),
                            title = stringResource(R.string.applications_kocolor_features_cosmetics_taxonomy_facets_title),
                            description = stringResource(R.string.applications_kocolor_features_cosmetics_taxonomy_facets_desc),
                            items = listOf(
                                "Formulation" to "Liquid, Cream, Powder, Gel, Balm.",
                                "Chemistry" to "Water, Silicone, or Oil bases (Critical for layering).",
                                "Finish" to "Matte, Satin, Radiant, Metallic, Glitter.",
                                "Coverage" to "Sheer, Light, Medium, Full, Buildable.",
                                "Temperature" to "Warm, Cool, Neutral, Olive (Engine alignment)."
                            )
                        ),
                        onEvent = {},
                        navTo = {}
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEvent) {
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_understand), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFF9F6F0)
    )
}

data class TaxonomySectionUiState(
    val level: String,
    val title: String,
    val description: String,
    val items: List<Pair<String, String>>,
    val modifier: Modifier = Modifier
)

@Composable
private fun TaxonomySection(
    uiState: TaxonomySectionUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(modifier = uiState.modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                text = uiState.level.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = uiState.description,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.7f)
            )
        }

        Surface(
            color = Color.White.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.items.forEach { (label, detail) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.alpha(0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProfessionalTaxonomyDialogPreview() {
    MaterialTheme {
        ProfessionalTaxonomyDialog(
            uiState = ProfessionalTaxonomyUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}
