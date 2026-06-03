package com.zoewave.probase.kocolor.features.stitch.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.stitch.R
import com.zoewave.probase.kocolor.features.stitch.ui.components.SectionHeader
import com.zoewave.probase.kocolor.features.stitch.ui.components.StitchItemPickerOverlay
import com.zoewave.probase.kocolor.features.stitch.ui.components.StitchItemRow
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun StitchScreenPreview() {
    MaterialTheme {
        StitchScreen(
            uiState = StitchUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchScreen(
    uiState: StitchUiState,
    onEvent: (StitchEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val advice = uiState.draftAdvice ?: return
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_stitch_editor_title), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_stitch_back))
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        onEvent(StitchEvent.SaveCollection)
                        navTo(KoColorRoute.Back)
                    }) {
                        Text(stringResource(R.string.applications_kocolor_features_stitch_save), fontWeight = FontWeight.Black)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // 1. Metadata Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = advice.title ?: "",
                            onValueChange = { onEvent(StitchEvent.UpdateTitle(it)) },
                            label = { Text(stringResource(R.string.applications_kocolor_features_stitch_collection_title)) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                            shape = RoundedCornerShape(16.dp)
                        )
                        OutlinedTextField(
                            value = advice.summary,
                            onValueChange = { onEvent(StitchEvent.UpdateSummary(it)) },
                            label = { Text(stringResource(R.string.applications_kocolor_features_stitch_stylist_rationale)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                // 2. The Wardrobe (Outfit) Section
                item {
                    SectionHeader(stringResource(R.string.applications_kocolor_features_stitch_wardrobe_section), Icons.Default.Checkroom)
                }

                advice.outfitSuggestions.forEachIndexed { outfitIdx, outfit ->
                    items(outfit.suggestedItems.size) { itemIdx ->
                        val item = outfit.suggestedItems[itemIdx]
                        StitchItemRow(
                            title = item.name,
                            category = item.category,
                            imageUrl = item.imageUrl,
                            isOwned = item.isOwned,
                            onPickClick = { onEvent(StitchEvent.RequestPickItem(PickingTarget.Outfit(outfitIdx, itemIdx))) },
                            onRemoveClick = { onEvent(StitchEvent.RemoveOutfitItem(outfitIdx, itemIdx)) }
                        )
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { onEvent(StitchEvent.AddOutfitSlot) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.applications_kocolor_features_stitch_add_wardrobe_piece))
                    }
                }

                // 3. The Vanity Section
                item {
                    SectionHeader(stringResource(R.string.applications_kocolor_features_stitch_vanity_section), Icons.Default.Face)
                }

                items(advice.makeupSuggestions.size) { index ->
                    val makeup = advice.makeupSuggestions[index]
                    StitchItemRow(
                        title = makeup.suggestedProductName ?: makeup.category,
                        category = makeup.category,
                        imageUrl = makeup.suggestedProductImageUrl,
                        isOwned = makeup.productId != null,
                        onPickClick = { onEvent(StitchEvent.RequestPickItem(PickingTarget.Makeup(index))) },
                        onRemoveClick = { onEvent(StitchEvent.RemoveMakeupSuggestion(index)) }
                    )
                }

                item {
                    OutlinedButton(
                        onClick = { onEvent(StitchEvent.AddMakeupSlot) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.applications_kocolor_features_stitch_add_vanity_product))
                    }
                }

                // Danger Zone
                if (uiState.collectionId != 0L) {
                    item {
                        TextButton(
                            onClick = { 
                                onEvent(StitchEvent.DeleteCollection(uiState.collectionId))
                                navTo(KoColorRoute.Back)
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.applications_kocolor_features_stitch_delete_collection))
                        }
                    }
                }
                
                item { Spacer(Modifier.height(100.dp)) }
            }

            // OVERLAY: The Item Picker
            AnimatedVisibility(
                visible = uiState.pickingTarget != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                StitchItemPickerOverlay(
                    uiState = uiState,
                    onItemSelected = { onEvent(StitchEvent.OnItemSelected(it)) },
                    onDismiss = { onEvent(StitchEvent.ClearPickingTarget) }
                )
            }
        }
    }
}
