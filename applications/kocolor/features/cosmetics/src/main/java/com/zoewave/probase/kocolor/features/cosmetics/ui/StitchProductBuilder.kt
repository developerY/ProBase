package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.CosmeticCategory
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchProductBuilder(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val draft = uiState.draftItem
    
    val bgColor = draft.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surface
    val isDark = if (draft.colorHex != null) isColorDark(bgColor) else false
    val contentColor = if (isDark) Color.White else Color.Black

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("New Discovery", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
                        Text("Step $currentStep of 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (currentStep > 1) currentStep-- else navTo(KoColorRoute.Back)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentStep == 3) {
                        TextButton(onClick = { 
                            onEvent(CosmeticsEvent.AddItem(draft))
                            navTo(KoColorRoute.Back)
                        }) {
                            Text("Stitch", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Step Progress Bar
            LinearProgressIndicator(
                progress = { currentStep / 3f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                }
            ) { step ->
                when (step) {
                    1 -> CaptureStep(draft, uiState, onEvent, navTo, onNext = { currentStep = 2 })
                    2 -> ColorStep(draft, uiState, onEvent, onNext = { currentStep = 3 })
                    3 -> MetadataStep(draft, uiState, onEvent)
                }
            }
        }
    }
}

@Composable
private fun CaptureStep(
    draft: CosmeticItem,
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("First, let's see it.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clickable { navTo(KoColorRoute.Camera("inventory_item")) },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (draft.imageUrl != null) {
                    AsyncImage(
                        model = draft.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.padding(12.dp), tint = Color.White)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Capture Product", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (draft.imageUrl != null) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Looks Good", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ColorStep(
    draft: CosmeticItem,
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Extracting the essence.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        val bgColor = draft.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.primary
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(bgColor)
                .clickable { /* Future: Color Picker */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = draft.shadeName ?: "Discovery",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isColorDark(bgColor)) Color.White else Color.Black,
                fontWeight = FontWeight.Black
            )
        }

        Text("Gemini AI is analyzing the color profile...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("#FAD4D4", "#F8F0E3", "#F0C080", "#E0AC69").forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(parseColor(hex))
                        .border(if (draft.colorHex == hex) 4.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(colorHex = hex))) }
                )
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MetadataStep(
    draft: CosmeticItem,
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Final details.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(
            value = draft.name,
            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(name = it))) },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        OutlinedTextField(
            value = draft.brand,
            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(brand = it))) },
            label = { Text("Brand") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = draft.price?.toString() ?: "",
                onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(price = it.toDoubleOrNull()))) },
                label = { Text("Price") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                prefix = { Text("$") }
            )
            OutlinedTextField(
                value = draft.batchCode ?: "",
                onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(batchCode = it))) },
                label = { Text("Batch / SKU") },
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(16.dp)
            )
        }
        
        // Category Selection
        var showMenu by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(
                onClick = { showMenu = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(draft.category.displayName)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                CosmeticCategory.entries.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.displayName) }, onClick = { 
                        onEvent(CosmeticsEvent.UpdateDraft(draft.copy(category = cat)))
                        showMenu = false
                    })
                }
            }
        }
    }
}
