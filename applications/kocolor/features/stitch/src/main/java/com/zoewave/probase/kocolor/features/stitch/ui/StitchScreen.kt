package com.zoewave.probase.kocolor.features.stitch.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.stitch.R
import com.zoewave.probase.kocolor.model.KoColorRoute

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

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StitchItemRow(
    title: String,
    category: String,
    imageUrl: String?,
    isOwned: Boolean,
    onPickClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isOwned) Color.White else Color(0xFFF5F5F5)),
        border = if (isOwned) BorderStroke(1.dp, Color(0xFFEEEEEE)) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.LightGray.copy(alpha = 0.2f)
            ) {
                if (imageUrl != null) {
                    AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = category.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (!isOwned) {
                    Text(stringResource(R.string.applications_kocolor_features_stitch_suggested_piece), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            IconButton(onClick = onPickClick) {
                Icon(Icons.Default.SwapHoriz, stringResource(R.string.applications_kocolor_features_stitch_change_item), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Close, stringResource(R.string.applications_kocolor_features_stitch_remove_slot), modifier = Modifier.size(20.dp), tint = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StitchItemPickerOverlay(
    uiState: StitchUiState,
    onItemSelected: (Any) -> Unit,
    onDismiss: () -> Unit
) {
    val isMakeup = uiState.pickingTarget is PickingTarget.Makeup
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text(if (isMakeup) stringResource(R.string.applications_kocolor_features_stitch_select_cosmetic) else stringResource(R.string.applications_kocolor_features_stitch_select_garment), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isMakeup) {
                    items(uiState.allCosmetics) { item ->
                        PickerItemRow(item.name, item.brand, item.imageUrl, item.microCategory.displayName) { onItemSelected(item) }
                    }
                } else {
                    items(uiState.allWardrobe) { item ->
                        PickerItemRow(item.name, item.brand ?: stringResource(R.string.applications_kocolor_features_stitch_archive), item.imageUrl, item.category.name) { onItemSelected(item) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerItemRow(title: String, subtitle: String, imageUrl: String?, cat: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFF5F5F5)) {
            if (imageUrl != null) AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = cat, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
