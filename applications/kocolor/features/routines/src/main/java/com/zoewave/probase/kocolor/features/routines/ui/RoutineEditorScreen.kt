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
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<CosmeticCategory?>(null) }

    val activeStep = routine.steps.find { it.id == editingStepId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = when (selectionStage) {
                            ProductSelectionStage.MainForm -> if (editingStepId == null) "Curate Ritual" else "Edit Step"
                            ProductSelectionStage.Group -> "Select Group"
                            ProductSelectionStage.Category -> "Select Category"
                            ProductSelectionStage.Item -> "Select Product"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        when (selectionStage) {
                            ProductSelectionStage.MainForm -> {
                                if (editingStepId != null) editingStepId = null else onBack()
                            }
                            ProductSelectionStage.Group -> selectionStage = ProductSelectionStage.MainForm
                            ProductSelectionStage.Category -> selectionStage = ProductSelectionStage.Group
                            ProductSelectionStage.Item -> selectionStage = ProductSelectionStage.Category
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
                            onClick = { /* Logic for adding new step entity */ },
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Ritual Stage", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (activeStep != null) {
                when (selectionStage) {
                    ProductSelectionStage.MainForm -> {
                        EditStepForm(
                            step = activeStep,
                            allProducts = uiState.allProducts,
                            onProductClick = { selectionStage = ProductSelectionStage.Group },
                            onRemoveStep = { 
                                onEvent(RoutinesEvent.RemoveStep(routine.id, activeStep.id))
                                editingStepId = null
                            }
                        )
                    }
                    ProductSelectionStage.Group -> {
                        GroupSelectionPage(uiState.allProducts) { group ->
                            selectedGroup = group
                            selectionStage = ProductSelectionStage.Category
                        }
                    }
                    ProductSelectionStage.Category -> {
                        CategorySelectionPage(
                            allProducts = uiState.allProducts.filter { it.category.groupName == selectedGroup }
                        ) { category ->
                            selectedCategory = category
                            selectionStage = ProductSelectionStage.Item
                        }
                    }
                    ProductSelectionStage.Item -> {
                        ItemSelectionPage(
                            products = uiState.allProducts.filter { it.category == selectedCategory },
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

enum class ProductSelectionStage { MainForm, Group, Category, Item }

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
                    text = "Ritual Stage ${step.layeringOrder + 1}", 
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
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // 1. Hero Visual
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF1F3F0))
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

        // 2. Field: Step Title
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Step Title", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Black, 
                    modifier = Modifier.alpha(0.4f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.alpha(0.1f))
            }
        }

        // 3. Field: Product Used
        item {
            Column(
                modifier = Modifier.clickable { onProductClick() }.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Product Used", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Black, 
                    modifier = Modifier.alpha(0.4f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = linkedProduct?.name ?: "Select a product...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (linkedProduct == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.alpha(0.1f))
            }
        }

        // 4. Card: Timing & Reminders
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text("Timing & Reminders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Duration (Min)", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold, 
                                modifier = Modifier.alpha(0.5f)
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "${step.minWaitMinutes}", fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(20.dp))
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
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "07:30 AM", fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp).alpha(0.6f))
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

        // 5. Field: How-to Instructions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "How-to Instructions", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Black, 
                    modifier = Modifier.alpha(0.4f),
                    letterSpacing = 1.sp
                )
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Apply 3-4 drops to clean, dry skin. Gently press into the face and neck until fully absorbed. Wait 2 minutes before applying moisturizer.",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 6. Action: Remove Step
        item {
            TextButton(
                onClick = onRemoveStep,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 48.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB03030))
            ) {
                Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Remove Ritual Stage", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GroupSelectionPage(
    allProducts: List<CosmeticItem>,
    onGroupClick: (String) -> Unit
) {
    val groups = allProducts.map { it.category.groupName }.distinct()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(groups) { group ->
            SelectionRow(text = group) { onGroupClick(group) }
        }
    }
}

@Composable
private fun CategorySelectionPage(
    allProducts: List<CosmeticItem>,
    onCategoryClick: (CosmeticCategory) -> Unit
) {
    val categories = allProducts.map { it.category }.distinct()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            SelectionRow(text = category.displayName) { onCategoryClick(category) }
        }
    }
}

@Composable
private fun SelectionRow(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.alpha(0.3f))
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
                    Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
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
