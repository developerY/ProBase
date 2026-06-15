package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.TaxonomyDropdown
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun ValidateItemScreenPreview() {
    MaterialTheme {
        ValidateItemScreen(
            uiState = CosmeticsUiState(
                draftItem = CosmeticItem(
                    name = "SuperStay Matte Ink",
                    brand = "Maybelline",
                    macroCategory = MacroCategory.LIPS,
                    microCategory = MicroCategory.LIPSTICK
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidateItemScreen(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val draft = uiState.draftItem
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_validate_item), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif, color = Color(0xFF8B5E3C)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(CosmeticsEvent.ResetScanState) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hero Image with Captured Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFF9F6F0))
            ) {
                if (draft.imageUrl != null) {
                    AsyncImage(
                        model = draft.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircleOutline, null, modifier = Modifier.size(16.dp), tint = Color(0xFF8B5E3C))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_captured_badge), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_scanned_details), style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_brand_name), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = draft.brand,
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(brand = it))) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_product_name), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(name = it))) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.applications_kocolor_features_cosmetics_category), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                            TaxonomyDropdown(
                                current = draft.macroCategory.displayName,
                                items = MacroCategory.entries.map { it.displayName },
                                onSelect = { name ->
                                    val cat = MacroCategory.entries.first { it.displayName == name }
                                    onEvent(CosmeticsEvent.UpdateDraft(draft.copy(macroCategory = cat, microCategory = MicroCategory.entries.first { it.macro == cat })))
                                }
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.applications_kocolor_features_cosmetics_subcategory), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                            TaxonomyDropdown(
                                current = draft.microCategory.displayName,
                                items = MicroCategory.entries.filter { it.macro == draft.macroCategory }.map { it.displayName },
                                onSelect = { name ->
                                    val micro = MicroCategory.entries.first { it.displayName == name }
                                    onEvent(CosmeticsEvent.UpdateDraft(draft.copy(microCategory = micro)))
                                }
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_shade_name), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = draft.shadeName ?: "",
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(shadeName = it))) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. Actions
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { 
                        onEvent(CosmeticsEvent.AddItem(draft))
                        onEvent(CosmeticsEvent.ResetScanState)
                        navTo(KoColorRoute.Back)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5E3C))
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_confirm_and_add), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                OutlinedButton(
                    onClick = { 
                        onEvent(CosmeticsEvent.ResetScanState)
                        navTo(KoColorRoute.BarcodeScanner) 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_rescan), color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
