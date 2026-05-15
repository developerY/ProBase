package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.CosmeticCategory
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun CosmeticsUiRoute(
    uiState: Unit = Unit,
    onEvent: (Unit) -> Unit = {},
    navTo: (KoColorRoute) -> Unit
) {
    val viewModel: CosmeticsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CosmeticsScreen(
        uiState = state,
        onEvent = viewModel::onEvent,
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

    // Re-open dialog if coming back from camera
    LaunchedEffect(uiState.capturedImageUri) {
        if (uiState.capturedImageUri != null) {
            showAddDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cosmetic Inventory") },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
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

                val groupedBySection = remember(uiState.items) {
                    uiState.items.groupBy { it.category.groupName }
                }

                val expandedGroups = remember { 
                    mutableStateMapOf<String, Boolean>().apply {
                        allGroups.forEach { put(it, false) }
                    }
                }

                val expandedSubgroups = remember {
                    mutableStateMapOf<CosmeticCategory, Boolean>().apply {
                        CosmeticCategory.entries.forEach { put(it, false) }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    allGroups.forEach { groupName ->
                        val itemsInGroup = groupedBySection[groupName] ?: emptyList()
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
                                                    text = "No items added yet. Tap + to start your collection!",
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
                                                onEvent = { event ->
                                                    when (event) {
                                                        "delete" -> onEvent(CosmeticsEvent.DeleteItem(item.id))
                                                        "edit" -> selectedItemForEdit = item
                                                    }
                                                },
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

    selectedItemForEdit?.let { item ->
        EditCosmeticDialog(
            uiState = item,
            onEvent = { onEvent(CosmeticsEvent.UpdateItem(it)) },
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
    onEvent: (String) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = uiState.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surface
    val contentColor = if (uiState.colorHex != null) {
        if (isColorDark(cardColor)) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEvent("edit") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Text(
                    text = uiState.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = contentColor
                )
                Text(
                    text = uiState.brand,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
                uiState.shadeName?.let { shade ->
                    Surface(
                        color = contentColor.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = shade,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = contentColor
                        )
                    }
                }
            }
            
            IconButton(onClick = { onEvent("delete") }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (uiState.colorHex != null) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}

@Composable
fun FullColorPicker(
    uiState: String,
    onEvent: (String) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val controller = rememberColorPickerController(initialColor = parseColor(uiState))

    AlertDialog(
        onDismissRequest = { navTo(KoColorRoute.Back) },
        title = { Text("Pick Custom Color", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Preview Tile
                AlphaTile(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    controller = controller
                )

                // The Main HSV Color Wheel/Picker
                HsvColorPicker(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(10.dp),
                    controller = controller
                )

                // Brightness Slider
                Column {
                    Text("Brightness", style = MaterialTheme.typography.labelSmall)
                    BrightnessSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp),
                        controller = controller
                    )
                }

                Text(
                    text = String.format("#%06X", (0xFFFFFF and controller.selectedColor.value.toArgb())),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                val hex = String.format("#%06X", (0xFFFFFF and controller.selectedColor.value.toArgb()))
                onEvent(hex)
                navTo(KoColorRoute.Back)
            }) {
                Text("Select Color")
            }
        },
        dismissButton = {
            TextButton(onClick = { navTo(KoColorRoute.Back) }) { Text("Cancel") }
        }
    )
}

class ColorPickerController(initialColor: Color) {
    var selectedColor = mutableStateOf(initialColor)
    
    // Internal HSV state for the picker logic
    var hue = mutableFloatStateOf(0f)
    var saturation = mutableFloatStateOf(0f)
    var value = mutableFloatStateOf(1f)

    init {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(
            android.graphics.Color.rgb(
                (initialColor.red * 255).toInt(),
                (initialColor.green * 255).toInt(),
                (initialColor.blue * 255).toInt()
            ),
            hsv
        )
        hue.floatValue = hsv[0]
        saturation.floatValue = hsv[1]
        value.floatValue = hsv[2]
    }

    fun updateFromHsv() {
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue.floatValue, saturation.floatValue, value.floatValue))
        selectedColor.value = Color(argb)
    }
}

@Composable
fun rememberColorPickerController(initialColor: Color): ColorPickerController {
    return remember(initialColor) { ColorPickerController(initialColor) }
}

@Composable
fun AlphaTile(
    modifier: Modifier,
    controller: ColorPickerController
) {
    Box(
        modifier = modifier.background(controller.selectedColor.value)
    )
}

@Composable
fun HsvColorPicker(
    modifier: Modifier,
    controller: ColorPickerController
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val center = Offset(w / 2f, h / 2f)
                    val pos = change.position
                    
                    val dx = pos.x - center.x
                    val dy = pos.y - center.y
                    val radius = w / 2f
                    
                    val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    val angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    
                    controller.hue.floatValue = (if (angle < 0) angle + 360 else angle)
                    controller.saturation.floatValue = (dist / radius).coerceIn(0f, 1f)
                    controller.updateFromHsv()
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val radius = w / 2f
        val center = Offset(w / 2f, h / 2f)
        
        // Draw the color wheel background (simple radial sweep)
        for (i in 0 until 360) {
            val angle = Math.toRadians(i.toDouble()).toFloat()
            val start = center
            val end = Offset(
                center.x + Math.cos(angle.toDouble()).toFloat() * radius,
                center.y + Math.sin(angle.toDouble()).toFloat() * radius
            )
            val hsvColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f)))
            
            drawLine(
                brush = Brush.linearGradient(listOf(Color.White, hsvColor), start, end),
                start = start,
                end = end,
                strokeWidth = 2f
            )
        }
        
        // Draw the indicator
        val indicatorAngle = Math.toRadians(controller.hue.floatValue.toDouble()).toFloat()
        val indicatorRadius = controller.saturation.floatValue * radius
        val indicatorOffset = Offset(
            center.x + Math.cos(indicatorAngle.toDouble()).toFloat() * indicatorRadius,
            center.y + Math.sin(indicatorAngle.toDouble()).toFloat() * indicatorRadius
        )
        
        drawCircle(
            color = if (controller.value.floatValue > 0.5f) Color.Black else Color.White,
            radius = 8.dp.toPx(),
            center = indicatorOffset,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun BrightnessSlider(
    modifier: Modifier,
    controller: ColorPickerController
) {
    Slider(
        value = controller.value.floatValue,
        onValueChange = { 
            controller.value.floatValue = it
            controller.updateFromHsv()
        },
        valueRange = 0f..1f,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = controller.selectedColor.value,
            activeTrackColor = controller.selectedColor.value.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun EditCosmeticDialog(
    uiState: CosmeticItem,
    onEvent: (CosmeticItem) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var name by remember { mutableStateOf(uiState.name) }
    var brand by remember { mutableStateOf(uiState.brand) }
    var category by remember { mutableStateOf(uiState.category) }
    var colorHex by remember { mutableStateOf(uiState.colorHex ?: "") }
    var shadeName by remember { mutableStateOf(uiState.shadeName ?: "") }
    var notes by remember { mutableStateOf(uiState.notes ?: "") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        FullColorPicker(
            uiState = colorHex,
            onEvent = { colorHex = it },
            navTo = { showColorPicker = false }
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
                // Image Capture / Preview (Reusing existing imageUrl or capturing new one)
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
                        val imageUrl = uiState.imageUrl
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showCategoryMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category.displayName)
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
                                    color = MaterialTheme.colorScheme.primary
                                )
                                items.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.displayName) },
                                        onClick = {
                                            category = cat
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = colorHex,
                        onValueChange = { colorHex = it },
                        label = { Text("Color Hex") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showColorPicker = true }
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        color = try { 
                            if (colorHex.isNotEmpty()) Color(android.graphics.Color.parseColor(colorHex)) else Color.Transparent 
                        } catch (e: Exception) { Color.Transparent }
                    ) {
                        if (colorHex.isEmpty()) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = "Pick Color",
                                modifier = Modifier.padding(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = shadeName,
                    onValueChange = { shadeName = it },
                    label = { Text("Shade Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onEvent(uiState.copy(
                        name = name,
                        brand = brand,
                        category = category,
                        colorHex = colorHex.takeIf { it.isNotBlank() },
                        shadeName = shadeName.takeIf { it.isNotBlank() },
                        notes = notes.takeIf { it.isNotBlank() }
                    ))
                    navTo(KoColorRoute.Back)
                },
                enabled = name.isNotBlank() && brand.isNotBlank()
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
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CosmeticCategory.FOUNDATION) }
    var colorHex by remember { mutableStateOf("") }
    var shadeName by remember { mutableStateOf("") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    // Auto-fill from AI result
    LaunchedEffect(uiState.aiResult) {
        uiState.aiResult?.let { result ->
            name = result.name
            brand = result.brand
            category = result.category
            colorHex = result.colorHex ?: ""
            shadeName = result.shadeName ?: ""
        }
    }

    if (showColorPicker) {
        FullColorPicker(
            uiState = colorHex,
            onEvent = { colorHex = it },
            navTo = { showColorPicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = { navTo(KoColorRoute.Back) },
        title = { Text("Add Cosmetic Item") },
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
                        if (uiState.capturedImageUri != null) {
                            AsyncImage(
                                model = uiState.capturedImageUri,
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
                        enabled = !uiState.isAnalyzing
                    ) {
                        if (uiState.isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auto-fill with Gemini")
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showCategoryMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category.displayName)
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
                                    color = MaterialTheme.colorScheme.primary
                                )
                                items.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.displayName) },
                                        onClick = {
                                            category = cat
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = colorHex,
                        onValueChange = { colorHex = it },
                        label = { Text("Color Hex") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showColorPicker = true }
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        color = try { 
                            if (colorHex.isNotEmpty()) Color(android.graphics.Color.parseColor(colorHex)) else Color.Transparent 
                        } catch (e: Exception) { Color.Transparent }
                    ) {
                        if (colorHex.isEmpty()) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = "Pick Color",
                                modifier = Modifier.padding(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = shadeName,
                    onValueChange = { shadeName = it },
                    label = { Text("Shade Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onEvent(CosmeticsEvent.AddItem(
                        CosmeticItem(
                            name = name, 
                            brand = brand, 
                            category = category, 
                            colorHex = colorHex.takeIf { it.isNotBlank() }, 
                            shadeName = shadeName.takeIf { it.isNotBlank() },
                            imageUrl = uiState.capturedImageUri
                        )
                    ))
                    navTo(KoColorRoute.Back)
                },
                enabled = name.isNotBlank() && brand.isNotBlank()
            ) {
                Text("Add to Inventory")
            }
        },
        dismissButton = {
            TextButton(onClick = { navTo(KoColorRoute.Back) }) { Text("Cancel") }
        }
    )
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
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
            uiState = CosmeticItem(
                name = "Velvet Matte",
                brand = "Sample Brand",
                category = CosmeticCategory.LIPSTICK,
                colorHex = "#FF0000",
                shadeName = "True Red",
                notes = "Long-lasting finish."
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
private fun FullColorPickerPreview() {
    MaterialTheme {
        FullColorPicker(
            uiState = "#FF0000",
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
