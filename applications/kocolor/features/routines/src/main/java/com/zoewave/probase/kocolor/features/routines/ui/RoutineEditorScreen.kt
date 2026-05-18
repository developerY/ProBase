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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    uiState: RoutinesUiState,
    onEvent: (RoutinesEvent) -> Unit,
    onBack: () -> Unit
) {
    val routine = uiState.activeEditRoutine ?: return
    var editingStepId by remember { mutableStateOf<String?>(null) }
    
    // Multi-stage product selection state
    var selectionStage by remember { mutableStateOf<ProductSelectionStage>(ProductSelectionStage.MainForm) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val activeStep = routine.steps.find { it.id == editingStepId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = if (selectionStage == ProductSelectionStage.MainForm) "Edit Step" else "Select Product",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (selectionStage != ProductSelectionStage.MainForm) {
                            selectionStage = ProductSelectionStage.MainForm
                        } else if (editingStepId != null) {
                            editingStepId = null
                        } else {
                            onBack()
                        }
                    }) { 
                        Icon(
                            if (selectionStage == ProductSelectionStage.MainForm && editingStepId == null) Icons.Default.Close 
                            else Icons.AutoMirrored.Filled.ArrowBack, 
                            null
                        ) 
                    }
                },
                actions = {
                    if (selectionStage == ProductSelectionStage.MainForm) {
                        TextButton(onClick = { onEvent(RoutinesEvent.CloseEditDialog); onBack() }) {
                            Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (editingStepId == null) {
                // Step List Overview
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(routine.steps) { step ->
                        StepSummaryRow(step) { editingStepId = step.id }
                    }
                    item {
                        OutlinedButton(
                            onClick = { /* Add Step Logic */ },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add New Step")
                        }
                    }
                }
            } else if (activeStep != null) {
                when (selectionStage) {
                    ProductSelectionStage.MainForm -> {
                        EditStepForm(
                            step = activeStep,
                            allProducts = uiState.allProducts,
                            onProductClick = { selectionStage = ProductSelectionStage.Category },
                            onRemoveStep = { 
                                onEvent(RoutinesEvent.RemoveStep(routine.id, activeStep.id))
                                editingStepId = null
                            }
                        )
                    }
                    ProductSelectionStage.Category -> {
                        CategorySelectionPage(uiState.allProducts) { category ->
                            selectedCategory = category
                            selectionStage = ProductSelectionStage.Item
                        }
                    }
                    ProductSelectionStage.Item -> {
                        ItemSelectionPage(
                            products = uiState.allProducts.filter { it.category.groupName == selectedCategory },
                            selectedIds = activeStep.productIds
                        ) { productId ->
                            onEvent(RoutinesEvent.LinkProduct(routine.id, activeStep.id, productId))
                            selectionStage = ProductSelectionStage.MainForm
                        }
                    }
                }
            }
        }
    }
}

enum class ProductSelectionStage { MainForm, Category, Item }

@Composable
private fun StepSummaryRow(step: RoutineStep, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Step ${step.layeringOrder + 1}", 
                    style = MaterialTheme.typography.labelSmall, 
                    modifier = Modifier.alpha(0.5f)
                )
            }
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.alpha(0.3f))
        }
    }
}

@Composable
private fun EditStepForm(
    step: RoutineStep,
    allProducts: List<CosmeticItem>,
    onProductClick: () -> Unit,
    onRemoveStep: () -> Unit
) {
    val linkedProduct = allProducts.find { step.productIds.contains(it.id) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE5E7E1)) // Placeholder background
            ) {
                if (linkedProduct?.imageUrl != null) {
                    AsyncImage(
                        model = linkedProduct.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AutoAwesome, 
                        null, 
                        modifier = Modifier.size(64.dp).align(Alignment.Center).alpha(0.1f)
                    )
                }
            }
        }

        // Form Fields
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Step Title", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.alpha(0.5f)
                )
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.alpha(0.1f))
            }
        }

        item {
            Column(
                modifier = Modifier.clickable { onProductClick() }.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Product Used", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.alpha(0.5f)
                )
                Text(
                    text = linkedProduct?.name ?: "Select a product...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (linkedProduct == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.alpha(0.1f))
            }
        }

        // Timing & Reminders Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text("Timing & Reminders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Duration (Min)", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold, 
                                modifier = Modifier.alpha(0.5f)
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "${step.minWaitMinutes}")
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reminder Time", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold, 
                                modifier = Modifier.alpha(0.5f)
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "07:30 AM")
                                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Notification", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Get a push notification for this step.", 
                                style = MaterialTheme.typography.bodySmall, 
                                modifier = Modifier.alpha(0.5f)
                            )
                        }
                        Switch(checked = true, onCheckedChange = {})
                    }
                }
            }
        }

        // How-to Instructions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "How-to Instructions", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.alpha(0.5f)
                )
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Apply 3-4 drops to clean, dry skin. Gently press into the face and neck until fully absorbed. Wait 2 minutes before applying moisturizer.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Remove Step
        item {
            TextButton(
                onClick = onRemoveStep,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB03030))
            ) {
                Icon(Icons.Default.DeleteOutline, null)
                Spacer(Modifier.width(8.dp))
                Text("Remove Step")
            }
        }
    }
}

@Composable
private fun CategorySelectionPage(
    allProducts: List<CosmeticItem>,
    onCategoryClick: (String) -> Unit
) {
    val categories = allProducts.map { it.category.groupName }.distinct()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            Surface(
                onClick = { onCategoryClick(category) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null, modifier = Modifier.alpha(0.3f))
                }
            }
        }
    }
}

@Composable
private fun ItemSelectionPage(
    products: List<CosmeticItem>,
    selectedIds: List<Long>,
    onItemClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { product ->
            val isSelected = selectedIds.contains(product.id)
            Surface(
                onClick = { onItemClick(product.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.05f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        if (product.imageUrl != null) {
                            AsyncImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(text = product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = product.brand, 
                            style = MaterialTheme.typography.labelSmall, 
                            modifier = Modifier.alpha(0.5f)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
