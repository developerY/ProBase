package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.*

data class RoutineEditorUiState(
    val initialStepId: String? = null,
    val routinesUiState: RoutinesUiState,
    val onBack: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    uiState: RoutineEditorUiState,
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val initialStepId = uiState.initialStepId
    val state = uiState.routinesUiState
    val onBack = uiState.onBack ?: {}
    
    val routine = state.activeEditRoutine ?: return
    var editingStepId by remember { mutableStateOf(initialStepId) }
    
    var newStepDraft by remember { 
        mutableStateOf(
            if (initialStepId == "new_step") {
                RoutineStep(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "New Ritual Stage",
                    layeringOrder = routine.steps.size
                )
            } else null
        )
    }

    var selectionStage by remember { 
        mutableStateOf(if (initialStepId == "new_step") ProductSelectionStage.MainForm else ProductSelectionStage.HeroPage) 
    }

    var selectedMacro by remember { mutableStateOf<MacroCategory?>(null) }
    var selectedMicro by remember { mutableStateOf<MicroCategory?>(null) }

    val activeStep = newStepDraft ?: routine.steps.find { it.id == editingStepId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = when (selectionStage) {
                            ProductSelectionStage.HeroPage -> if (editingStepId == null) "Curate Ritual" else "Ritual Knowledge"
                            ProductSelectionStage.MainForm -> if (newStepDraft != null) "New Stage" else "Edit Stage"
                            ProductSelectionStage.Macro -> "Select Category"
                            ProductSelectionStage.Micro -> "Select Type"
                            ProductSelectionStage.Item -> "Select Product"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        when (selectionStage) {
                            ProductSelectionStage.HeroPage -> { if (editingStepId != null) editingStepId = null else onBack() }
                            ProductSelectionStage.MainForm -> { if (newStepDraft != null) onBack() else selectionStage = ProductSelectionStage.HeroPage }
                            ProductSelectionStage.Macro -> selectionStage = ProductSelectionStage.MainForm
                            ProductSelectionStage.Micro -> selectionStage = ProductSelectionStage.Macro
                            ProductSelectionStage.Item -> selectionStage = ProductSelectionStage.Micro
                        }
                    }) { 
                        Icon(if (selectionStage == ProductSelectionStage.HeroPage && editingStepId == null) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, null) 
                    }
                },
                actions = {
                    if (selectionStage == ProductSelectionStage.HeroPage || selectionStage == ProductSelectionStage.MainForm) {
                        TextButton(onClick = { 
                            if (newStepDraft != null) {
                                onEvent(RoutinesEvent.UpdateRoutine(routine.copy(steps = routine.steps + newStepDraft!!)))
                            }
                            onEvent(RoutinesEvent.CloseEditDialog); onBack() 
                        }) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (editingStepId == null && newStepDraft == null) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(routine.steps) { step ->
                        StepSummaryRow(uiState = step, onEvent = { editingStepId = step.id; selectionStage = ProductSelectionStage.HeroPage })
                    }
                }
            } else if (activeStep != null) {
                when (selectionStage) {
                    ProductSelectionStage.HeroPage -> StepHeroPage(uiState = activeStep to state.allProducts, onEvent = { selectionStage = ProductSelectionStage.MainForm })
                    ProductSelectionStage.MainForm -> EditStepForm(uiState = activeStep to state.allProducts, onEvent = { event ->
                        when (event) {
                            "product" -> selectionStage = ProductSelectionStage.Macro
                            "remove" -> { if (newStepDraft != null) onBack() else { onEvent(RoutinesEvent.RemoveStep(routine.id, activeStep.id)); editingStepId = null } }
                        }
                    })
                    ProductSelectionStage.Macro -> MacroSelectionPage(onEvent = { macro -> selectedMacro = macro; selectionStage = ProductSelectionStage.Micro })
                    ProductSelectionStage.Micro -> MicroSelectionPage(macro = selectedMacro!!, onEvent = { micro -> selectedMicro = micro; selectionStage = ProductSelectionStage.Item })
                    ProductSelectionStage.Item -> ItemSelectionPage(uiState = Triple(state.allProducts.filter { it.microCategory == selectedMicro }, activeStep.productIds, { productId: Long ->
                        if (newStepDraft != null) {
                            val newIds = if (newStepDraft!!.productIds.contains(productId)) newStepDraft!!.productIds - productId else newStepDraft!!.productIds + productId
                            newStepDraft = newStepDraft!!.copy(productIds = newIds)
                        } else {
                            onEvent(RoutinesEvent.LinkProduct(routine.id, activeStep.id, productId))
                        }
                        selectionStage = ProductSelectionStage.MainForm
                    }))
                }
            }
        }
    }
}

enum class ProductSelectionStage { HeroPage, MainForm, Macro, Micro, Item }

@Composable
private fun StepSummaryRow(uiState: RoutineStep, onEvent: (Unit) -> Unit) {
    Surface(onClick = { onEvent(Unit) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = uiState.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Ritual Stage ${uiState.layeringOrder + 1}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.alpha(0.5f))
            }
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.alpha(0.3f))
        }
    }
}

@Composable
private fun StepHeroPage(uiState: Pair<RoutineStep, List<CosmeticItem>>, onEvent: (Unit) -> Unit) {
    val (step, allProducts) = uiState
    val linkedProduct = allProducts.find { step.productIds.contains(it.id) }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(32.dp)).background(Color(0xFFF1F3F0))) {
                if (linkedProduct?.imageUrl != null) AsyncImage(model = linkedProduct.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)), startY = 400f)))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    Text(text = "STAGE ${step.layeringOrder + 1}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text(text = step.title, style = MaterialTheme.typography.headlineLarge, color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (linkedProduct != null) {
            item {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(20.dp)) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            if (linkedProduct.imageUrl != null) AsyncImage(model = linkedProduct.imageUrl, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(text = linkedProduct.brand.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = linkedProduct.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                            Text(text = linkedProduct.microCategory.displayName, style = MaterialTheme.typography.labelSmall, modifier = Modifier.alpha(0.5f))
                        }
                    }
                }
            }
        }
        item {
            Button(onClick = { onEvent(Unit) }, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)) {
                Icon(Icons.Default.Tune, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("EDIT RITUAL STAGE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun EditStepForm(uiState: Pair<RoutineStep, List<CosmeticItem>>, onEvent: (String) -> Unit) {
    val (step, allProducts) = uiState
    val linkedProduct = allProducts.find { step.productIds.contains(it.id) }
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Step Title", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, modifier = Modifier.alpha(0.4f), letterSpacing = 1.sp)
            Text(text = step.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.alpha(0.1f))
        }
        Column(modifier = Modifier.clickable { onEvent("product") }.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Product Used", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, modifier = Modifier.alpha(0.4f), letterSpacing = 1.sp)
            Text(text = linkedProduct?.name ?: "Select a product...", style = MaterialTheme.typography.headlineSmall, color = if (linkedProduct == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            HorizontalDivider(modifier = Modifier.alpha(0.1f))
        }
    }
}

@Composable
private fun MacroSelectionPage(onEvent: (MacroCategory) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(MacroCategory.entries) { macro ->
            SelectionRow(text = macro.displayName, onClick = { onEvent(macro) })
        }
    }
}

@Composable
private fun MicroSelectionPage(macro: MacroCategory, onEvent: (MicroCategory) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(MicroCategory.entries.filter { it.macro == macro }) { micro ->
            SelectionRow(text = micro.displayName, onClick = { onEvent(micro) })
        }
    }
}

@Composable
private fun SelectionRow(text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.alpha(0.3f))
        }
    }
}

@Composable
private fun ItemSelectionPage(uiState: Triple<List<CosmeticItem>, List<Long>, (Long) -> Unit>) {
    val (products, selectedIds, onItemClick) = uiState
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(products) { product ->
            val isSelected = selectedIds.contains(product.id)
            Surface(onClick = { onItemClick(product.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.05f))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        if (product.imageUrl != null) AsyncImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(text = product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(text = product.brand, style = MaterialTheme.typography.labelSmall, modifier = Modifier.alpha(0.5f))
                    }
                    Spacer(Modifier.weight(1f))
                    if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
