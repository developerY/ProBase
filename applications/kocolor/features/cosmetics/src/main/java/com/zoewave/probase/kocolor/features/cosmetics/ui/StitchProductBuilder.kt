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
import com.zoewave.probase.kocolor.model.*
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog

import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
private fun StitchProductBuilderPreview() {
    MaterialTheme {
        StitchProductBuilder(
            uiState = CosmeticsUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchProductBuilder(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val draft = uiState.draftItem
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
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
                    1 -> CaptureStep(
                        uiState = draft to uiState,
                        onEvent = { currentStep = 2 },
                        navTo = navTo
                    )
                    2 -> ColorStep(
                        uiState = draft to uiState,
                        onEvent = { event ->
                            if (event is Unit) currentStep = 3
                            else if (event is CosmeticsEvent) onEvent(event)
                        },
                        navTo = {}
                    )
                    3 -> MetadataStep(
                        uiState = draft to uiState,
                        onEvent = onEvent,
                        navTo = navTo
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CaptureStepPreview() {
    MaterialTheme {
        val dummyItem = CosmeticItem(name = "", brand = "", macroCategory = MacroCategory.COMPLEXION, microCategory = MicroCategory.FOUNDATION)
        CaptureStep(
            uiState = dummyItem to CosmeticsUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
private fun CaptureStep(
    uiState: Pair<CosmeticItem, CosmeticsUiState>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val draft = uiState.first
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
                onClick = { onEvent(Unit) },
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
    uiState: Pair<CosmeticItem, CosmeticsUiState>,
    onEvent: (Any) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val draft = uiState.first
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        val colorHex = draft.colorHex ?: ""
        ColorPickerDialog(
            initialColor = try { parseColor(colorHex) } catch (e: Exception) { Color.Gray },
            onColorSelected = { 
                onEvent(CosmeticsEvent.UpdateDraft(draft.copy(colorHex = it.toHex()))) 
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false },
            title = "Pick Product Color"
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Extracting the essence.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        val bgColor = draft.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.primary
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(bgColor)
                .clickable { showColorPicker = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = draft.shadeName ?: "Discovery",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isColorDark(bgColor)) Color.White else Color.Black,
                fontWeight = FontWeight.Black
            )
        }

        Button(
            onClick = { onEvent(Unit) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MetadataStep(
    uiState: Pair<CosmeticItem, CosmeticsUiState>,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val draft = uiState.first
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        // Taxonomy: Progressive Disclosure
        TaxonomySelector(draft, onEvent)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TaxonomySelector(draft: CosmeticItem, onEvent: (CosmeticsEvent) -> Unit) {
    var showMacro by remember { mutableStateOf(false) }
    var showMicro by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Categorization", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
        
        // Level 1: Macro
        OutlinedButton(
            onClick = { showMacro = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(draft.macroCategory.displayName)
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }
        
        // Level 2: Micro
        OutlinedButton(
            onClick = { showMicro = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(draft.microCategory.displayName)
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }
    }

    DropdownMenu(expanded = showMacro, onDismissRequest = { showMacro = false }) {
        MacroCategory.entries.forEach { cat ->
            DropdownMenuItem(text = { Text(cat.displayName) }, onClick = {
                onEvent(CosmeticsEvent.UpdateDraft(draft.copy(
                    macroCategory = cat,
                    microCategory = MicroCategory.entries.first { it.macro == cat }
                )))
                showMacro = false
            })
        }
    }

    DropdownMenu(expanded = showMicro, onDismissRequest = { showMicro = false }) {
        MicroCategory.entries.filter { it.macro == draft.macroCategory }.forEach { cat ->
            DropdownMenuItem(text = { Text(cat.displayName) }, onClick = {
                onEvent(CosmeticsEvent.UpdateDraft(draft.copy(microCategory = cat)))
                showMicro = false
            })
        }
    }
}
