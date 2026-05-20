package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.kocolor.model.CosmeticCategory
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun CosmeticsUiRoutePreview() {
    MaterialTheme {
        CosmeticsUiRoute(
            uiState = CosmeticsUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun CosmeticsUiRoute(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    CosmeticsScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticsScreen(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showOrderDialog by remember { mutableStateOf(false) }
    var selectedItemForEdit by remember { mutableStateOf<CosmeticItem?>(null) }
    var showGuideForCategory by remember { mutableStateOf<CosmeticCategory?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Re-open dialog if coming back from camera
    LaunchedEffect(uiState.capturedImageUri) {
        if (uiState.capturedImageUri != null) {
            showAddDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.CosmeticAdd) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.name.lowercase().capitalize()) },
                                    onClick = {
                                        onEvent(CosmeticsEvent.UpdateSortOption(option))
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (uiState.sortOption == option) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showOrderDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Application Order")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(CosmeticsEvent.UpdateSearchQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by name, brand, or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    val allGroups = listOf(
                        "Face (Base & Coverage)",
                        "Cheeks (Color & Dimension)",
                        "Eyes (Definition)",
                        "Lips (Color & Texture)",
                        "Tools & Accessories"
                    )

                    val groupedBySection = remember(uiState.filteredItems) {
                        uiState.filteredItems.groupBy { it.category.groupName }
                    }

                    val expandedGroups = remember { 
                        mutableStateMapOf<String, Boolean>().apply {
                            allGroups.forEach { put(it, true) } // Expand all by default when searching
                        }
                    }

                    val expandedSubgroups = remember {
                        mutableStateMapOf<CosmeticCategory, Boolean>().apply {
                            CosmeticCategory.entries.forEach { put(it, true) }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        allGroups.forEach { groupName ->
                            val itemsInGroup = groupedBySection[groupName] ?: emptyList()
                            if (itemsInGroup.isEmpty() && uiState.searchQuery.isNotEmpty()) return@forEach

                            val isGroupExpanded = expandedGroups[groupName] == true
                            
                            item {
                                GroupSectionCard(
                                    uiState = Triple(groupName, itemsInGroup.size, isGroupExpanded),
                                    onEvent = { expandedGroups[groupName] = it },
                                    navTo = {}
                                )
                            }

                            if (isGroupExpanded) {
                                val categoriesInGroup = CosmeticCategory.entries.filter { it.groupName == groupName }
                                
                                categoriesInGroup.forEach { category ->
                                    val itemsInCategory = itemsInGroup.filter { it.category == category }
                                    if (itemsInCategory.isEmpty() && uiState.searchQuery.isNotEmpty()) return@forEach

                                    val isSubExpanded = expandedSubgroups[category] == true

                                    item {
                                        SubCategoryCard(
                                            uiState = Triple(category, itemsInCategory.size, isSubExpanded),
                                            onEvent = { event ->
                                                when (event) {
                                                    is Boolean -> expandedSubgroups[category] = event
                                                    "guide" -> showGuideForCategory = category
                                                }
                                            },
                                            navTo = {},
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }

                                    if (isSubExpanded) {
                                        if (itemsInCategory.isEmpty()) {
                                            item {
                                                Surface(
                                                    modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = "No items added yet.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(12.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            items(itemsInCategory) { item ->
                                                CosmeticProductCard(
                                                    uiState = item,
                                                    onEvent = onEvent,
                                                    navTo = navTo,
                                                    modifier = Modifier.padding(start = 32.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCosmeticDialog(
            uiState = uiState,
            onEvent = onEvent,
            navTo = { route ->
                if (route == KoColorRoute.Back) {
                    showAddDialog = false
                    onEvent(CosmeticsEvent.ClearCapturedImage)
                } else {
                    navTo(route)
                }
            }
        )
    }

    if (showOrderDialog) {
        MakeupOrderDialog(
            uiState = Unit,
            onEvent = {},
            navTo = { showOrderDialog = false }
        )
    }

    selectedItemForEdit?.let { _ ->
        EditCosmeticDialog(
            uiState = uiState,
            onEvent = onEvent,
            navTo = { route ->
                if (route == KoColorRoute.Back) {
                    selectedItemForEdit = null
                } else {
                    navTo(route)
                }
            }
        )
    }

    showGuideForCategory?.let { category ->
        CategoryGuideDialog(
            uiState = category,
            onEvent = {},
            navTo = { showGuideForCategory = null }
        )
    }
}

@Composable
fun GroupSectionCard(
    uiState: Triple<String, Int, Boolean>,
    onEvent: (Boolean) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val (title, itemCount, isExpanded) = uiState
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        onClick = { onEvent(!isExpanded) }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$itemCount items total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SubCategoryCard(
    uiState: Triple<CosmeticCategory, Int, Boolean>,
    onEvent: (Any) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val (category, itemCount, isExpanded) = uiState
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = { onEvent(!isExpanded) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onEvent("guide") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Help",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    }
                }
                Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                    Text(text = itemCount.toString(), modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
fun CategoryGuideDialog(
    uiState: CosmeticCategory,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    AlertDialog(
        onDismissRequest = { navTo(KoColorRoute.Back) },
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "${uiState.displayName} Guide", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "What is it?",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (uiState.suggestions.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Expert Tips & Suggestions",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.suggestions.forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { navTo(KoColorRoute.Back) }) { Text("Got it!") }
        }
    )
}

@Composable
fun CosmeticProductCard(
    uiState: CosmeticItem,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = uiState.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surface
    val contentColor = if (uiState.colorHex != null) {
        if (isColorDark(cardColor)) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val isExpiringSoon = uiState.estimatedExpiry?.let { expiry ->
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        (expiry - System.currentTimeMillis()) in 0..thirtyDaysInMillis
    } ?: false

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { navTo(KoColorRoute.CosmeticDetail(uiState.id)) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardColor,
            contentColor = contentColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Product Image or Swatch
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(contentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    val imageUrl = uiState.imageUrl
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = uiState.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = contentColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (isExpiringSoon) {
                            Icon(
                                Icons.Default.Warning, 
                                contentDescription = "Expiring Soon",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = uiState.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.shadeName?.let { shade ->
                            Surface(
                                color = contentColor.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = shade,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = contentColor
                                )
                            }
                        }
                        
                        if (uiState.isOpened) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "OPEN",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                IconButton(onClick = { onEvent(CosmeticsEvent.DeleteItem(uiState.id)) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (uiState.colorHex != null) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Professional Metrics
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("COST / USE", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
                        Text(
                            text = uiState.costPerUse?.let { "$%.2f".format(it) } ?: "N/A",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                    Column {
                        Text("USES", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
                        Text(
                            text = uiState.usageCount.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
                
                Button(
                    onClick = { onEvent(CosmeticsEvent.UseItem(uiState.id)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = contentColor.copy(alpha = 0.2f),
                        contentColor = contentColor
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("USE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun EditCosmeticDialog(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val draft = uiState.draftItem
    var showCategoryMenu by remember { mutableStateOf(false) }
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

    AlertDialog(
        onDismissRequest = { navTo(KoColorRoute.Back) },
        title = { Text("Product Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Capture / Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clickable { navTo(KoColorRoute.Camera("inventory_item")) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val imageUrl = draft.imageUrl
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Captured Product",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(32.dp))
                                Text("Update Product Photo", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(name = it))) },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = draft.brand,
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(brand = it))) },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Professional Inventory Row 1: Category & Price
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        OutlinedButton(
                            onClick = { showCategoryMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(draft.category.displayName, maxLines = 1)
                        }
                    }
                    OutlinedTextField(
                        value = draft.price?.toString() ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(price = it.toDoubleOrNull()))) },
                        label = { Text("Price ($)") },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true
                    )
                }

                // Professional Inventory Row 2: Shade & Color
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = draft.shadeName ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(shadeName = it))) },
                        label = { Text("Shade Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    val colorHex = draft.colorHex ?: ""
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showColorPicker = true }
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp)),
                        color = try { if (colorHex.isNotEmpty()) parseColor(colorHex) else Color.Transparent } catch (e: Exception) { Color.Transparent }
                    ) {
                        if (colorHex.isEmpty()) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.padding(12.dp))
                        }
                    }
                }

                // Professional Inventory Row 3: PAO & Batch Code
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draft.paoMonths?.toString() ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(paoMonths = it.toIntOrNull()))) },
                        label = { Text("PAO (Months)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navTo(KoColorRoute.BarcodeScanner) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Scan Barcode")
                        }
                        OutlinedButton(
                            onClick = { navTo(KoColorRoute.QRScanner) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Scan QR")
                        }
                    }

                    OutlinedTextField(
                        value = draft.batchCode ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(batchCode = it))) },
                        label = { Text("Batch Code / SKU") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = draft.notes ?: "",
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(notes = it))) },
                    label = { Text("Notes / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false },
                    modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 400.dp)
                ) {
                    CosmeticCategory.entries.filter { it != CosmeticCategory.AI_PENDING }.groupBy { it.groupName }.forEach { (group, items) ->
                        Text(
                            text = group,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        items.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    onEvent(CosmeticsEvent.UpdateDraft(draft.copy(category = cat)))
                                    showCategoryMenu = false
                                }
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onEvent(CosmeticsEvent.UpdateItem(draft))
                    navTo(KoColorRoute.Back)
                },
                enabled = draft.name.isNotBlank() && draft.brand.isNotBlank()
            ) {
                Text("Update Item")
            }
        },
        dismissButton = {
            TextButton(onClick = { navTo(KoColorRoute.Back) }) { Text("Cancel") }
        }
    )
}

@Composable
fun MakeupOrderDialog(
    uiState: Unit,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    AlertDialog(
        onDismissRequest = { navTo(KoColorRoute.Back) },
        title = { Text("Standard Application Order", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    OrderSection(
                        uiState = "Phase 1: Skin Prep" to "Skincare (Cleanser, toner, moisturizer, sunscreen) followed by Primer.",
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    OrderSection(
                        uiState = "Phase 2: The Base" to "Foundation/Skin Tint, then Concealer for blemishes, and Setting Powder for oily areas.",
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    OrderSection(
                        uiState = "Phase 3: Dimension & Cheeks" to "Apply Cream products (blush/bronzer) now, then Powder products, and Highlighter last.",
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    OrderSection(
                        uiState = "Phase 4: Eyes & Brows" to "Eyebrows first to frame the face, then Eyeshadow, Eyeliner, and finally Mascara.",
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    OrderSection(
                        uiState = "Phase 5: Lips & Setting" to "Lip Liner to shape, Lipstick/Gloss to fill, and Setting Spray to lock it all in.",
                        onEvent = {},
                        navTo = {}
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { navTo(KoColorRoute.Back) }) { Text("Got it!") }
        }
    )
}

@Composable
fun OrderSection(
    uiState: Pair<String, String>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column {
        Text(
            text = uiState.first,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = uiState.second,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AddCosmeticDialog(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val draft = uiState.draftItem
    var showCategoryMenu by remember { mutableStateOf(false) }
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

    AlertDialog(
        onDismissRequest = { navTo(KoColorRoute.Back) },
        title = { Text("Add Cosmetic Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Capture / Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clickable { navTo(KoColorRoute.Camera("inventory_item")) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val imageUrl = draft.imageUrl
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Captured Product",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(32.dp))
                                Text("Take Product Photo", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                if (uiState.capturedImageUri != null) {
                    Button(
                        onClick = { onEvent(CosmeticsEvent.ScanWithGemini) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isAnalyzing,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auto-fill with Gemini AI")
                        }
                    }
                }

                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(name = it))) },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = draft.brand,
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(brand = it))) },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Professional Inventory Row 1: Category & Price
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        OutlinedButton(
                            onClick = { showCategoryMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(draft.category.displayName, maxLines = 1)
                        }
                    }
                    OutlinedTextField(
                        value = draft.price?.toString() ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(price = it.toDoubleOrNull()))) },
                        label = { Text("Price ($)") },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true
                    )
                }

                // Professional Inventory Row 2: Shade & Color
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = draft.shadeName ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(shadeName = it))) },
                        label = { Text("Shade Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    val colorHex = draft.colorHex ?: ""
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showColorPicker = true }
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp)),
                        color = try { if (colorHex.isNotEmpty()) parseColor(colorHex) else Color.Transparent } catch (e: Exception) { Color.Transparent }
                    ) {
                        if (colorHex.isEmpty()) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.padding(12.dp))
                        }
                    }
                }

                // Professional Inventory Row 3: PAO & Batch Code
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draft.paoMonths?.toString() ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(paoMonths = it.toIntOrNull()))) },
                        label = { Text("PAO (Months)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navTo(KoColorRoute.BarcodeScanner) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Scan Barcode")
                        }
                        OutlinedButton(
                            onClick = { navTo(KoColorRoute.QRScanner) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Scan QR")
                        }
                    }

                    OutlinedTextField(
                        value = draft.batchCode ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(batchCode = it))) },
                        label = { Text("Batch Code / SKU") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false },
                    modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 400.dp)
                ) {
                    CosmeticCategory.entries.filter { it != CosmeticCategory.AI_PENDING }.groupBy { it.groupName }.forEach { (group, items) ->
                        Text(
                            text = group,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        items.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    onEvent(CosmeticsEvent.UpdateDraft(draft.copy(category = cat)))
                                    showCategoryMenu = false
                                }
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onEvent(CosmeticsEvent.AddItem(draft))
                    navTo(KoColorRoute.Back)
                },
                enabled = draft.name.isNotBlank() && draft.brand.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add to Inventory")
            }
        },
        dismissButton = {
            TextButton(onClick = { navTo(KoColorRoute.Back) }) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun OrderSectionPreview() {
    MaterialTheme {
        OrderSection(
            uiState = "Phase 1" to "Description here",
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MakeupOrderDialogPreview() {
    MaterialTheme {
        MakeupOrderDialog(
            uiState = Unit,
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddCosmeticDialogPreview() {
    MaterialTheme {
        AddCosmeticDialog(
            uiState = CosmeticsUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryGuideDialogPreview() {
    MaterialTheme {
        CategoryGuideDialog(
            uiState = CosmeticCategory.PRIMER,
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditCosmeticDialogPreview() {
    MaterialTheme {
        EditCosmeticDialog(
            uiState = CosmeticsUiState(
                draftItem = CosmeticItem(
                    name = "Velvet Matte",
                    brand = "Sample Brand",
                    category = CosmeticCategory.LIPSTICK,
                    colorHex = "#FF0000",
                    shadeName = "True Red",
                    notes = "Long-lasting finish."
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CosmeticProductCardPreview() {
    MaterialTheme {
        CosmeticProductCard(
            uiState = CosmeticItem(
                name = "Velvet Matte",
                brand = "Sample Brand",
                category = CosmeticCategory.LIPSTICK,
                colorHex = "#FF0000",
                shadeName = "True Red"
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupSectionCardPreview() {
    MaterialTheme {
        GroupSectionCard(
            uiState = Triple("Face", 12, true),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubCategoryCardPreview() {
    MaterialTheme {
        SubCategoryCard(
            uiState = Triple(CosmeticCategory.FOUNDATION, 3, false),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CosmeticsScreenPreview() {
    MaterialTheme {
        CosmeticsScreen(
            uiState = CosmeticsUiState(
                items = listOf(
                    CosmeticItem(name = "Luxe Glow", brand = "Brand A", category = CosmeticCategory.FOUNDATION),
                    CosmeticItem(name = "Night Berry", brand = "Brand B", category = CosmeticCategory.LIPSTICK, colorHex = "#800020")
                ),
                isLoading = false
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
