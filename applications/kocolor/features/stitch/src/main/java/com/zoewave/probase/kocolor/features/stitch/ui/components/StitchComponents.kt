package com.zoewave.probase.kocolor.features.stitch.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.stitch.R
import com.zoewave.probase.kocolor.features.stitch.ui.PickingTarget
import com.zoewave.probase.kocolor.features.stitch.ui.StitchUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

data class SectionHeaderUiState(val title: String, val icon: ImageVector)

@Composable
fun SectionHeader(
    uiState: SectionHeaderUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(uiState.icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(
            text = uiState.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

data class StitchItemRowUiState(
    val title: String,
    val category: String,
    val imageUrl: String?,
    val isOwned: Boolean,
    val modifier: Modifier = Modifier
)

@Composable
fun StitchItemRow(
    uiState: StitchItemRowUiState,
    onPickClick: () -> Unit,
    onRemoveClick: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Card(
        modifier = uiState.modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (uiState.isOwned) Color.White else Color(0xFFF5F5F5)),
        border = if (uiState.isOwned) BorderStroke(1.dp, Color(0xFFEEEEEE)) else null
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
                if (uiState.imageUrl != null) {
                    AsyncImage(model = uiState.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = uiState.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(text = uiState.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (!uiState.isOwned) {
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
fun StitchItemPickerOverlay(
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
                        PickerItemRow(
                            uiState = PickerItemUiState(item.name, item.brand, item.imageUrl, item.microCategory.displayName),
                            onEvent = { onItemSelected(item) },
                            navTo = {}
                        )
                    }
                } else {
                    items(uiState.allWardrobe) { item ->
                        PickerItemRow(
                            uiState = PickerItemUiState(item.name, item.brand ?: stringResource(R.string.applications_kocolor_features_stitch_archive), item.imageUrl, item.category.name),
                            onEvent = { onItemSelected(item) },
                            navTo = {}
                        )
                    }
                }
            }
        }
    }
}

data class PickerItemUiState(val title: String, val subtitle: String, val imageUrl: String?, val cat: String)

@Composable
fun PickerItemRow(
    uiState: PickerItemUiState, 
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = { onEvent(Unit) }).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFF5F5F5)) {
            if (uiState.imageUrl != null) AsyncImage(model = uiState.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = uiState.cat, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = uiState.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = uiState.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
