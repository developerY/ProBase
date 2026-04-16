package com.zoewave.probase.photodo.mobile.features.home.ui.components.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.home.R
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeEvent
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.HomeUiState
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.OverviewSummaryCard
import com.zoewave.probase.photodo.mobile.features.home.ui.components.home.components.bottomsheets.QuickTemplateBottomSheet
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeOverviewScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit, // ✅ Standardized Navigation Channel
    modifier: Modifier = Modifier
) {

    // Add these state variables at the top of your composable
    var showAddCategorySheet by rememberSaveable { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryOverviewUiModel?>(null) }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isSearchMode by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // 2. Replace the Scaffold's floatingActionButton block with this:
        floatingActionButton = {
            HomeOverviewFab(
                fabMenuExpanded = fabMenuExpanded,
                onFabToggle = { fabMenuExpanded = it },
                onAddCategoryClick = {
                    fabMenuExpanded = false
                    showAddCategorySheet = true
                },
                onHomeProjectClick = { // Updated parameter name
                    fabMenuExpanded = false
                    // Map to the common Quick Project logic with "Home" override!
                    onEvent(HomeEvent.OnAddQuickProjectClicked("Home"))
                },
                onCameraClick = {
                    fabMenuExpanded = false
                    navTo(PhotoTodoRoute.Camera(projectId = null))
                },
                onSmartCaptureClick = {
                    fabMenuExpanded = false
                    navTo(PhotoTodoRoute.SmartCapture())
                },
                isAiEnabled = uiState.isAiEnabled
            )
        }
    ) { innerPadding ->
        HomeOverviewContent(
            uiState = uiState,
            isSearchMode = isSearchMode,
            onSearchModeChange = { isSearchMode = it },
            onEvent = onEvent,
            navTo = navTo,
            onDeleteClicked = { categoryToDelete = it },
            modifier = Modifier.padding(innerPadding)
        )
    }

    // ... inside HomeOverviewScreen, just below the Scaffold closing brace ...

    HomeOverviewDialogs(
        showAddCategorySheet = showAddCategorySheet,
        onDismissAddCategory = { showAddCategorySheet = false },
        uiState = uiState, // Pass the UI State!
        categoryToDelete = categoryToDelete,
        onDismissDeleteConfirmation = { categoryToDelete = null },
        onEvent = onEvent
    )
} // <-- End of HomeOverviewScreen

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeOverviewFab(
    fabMenuExpanded: Boolean,
    onFabToggle: (Boolean) -> Unit,
    onAddCategoryClick: () -> Unit,
    onHomeProjectClick: () -> Unit, // Renamed
    onCameraClick: () -> Unit,
    onSmartCaptureClick: () -> Unit,
    isAiEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    FloatingActionButtonMenu(
        expanded = fabMenuExpanded,
        modifier = modifier,
        button = {
            ToggleFloatingActionButton(
                checked = fabMenuExpanded,
                onCheckedChange = onFabToggle
            ) {
                val imageVector by remember {
                    derivedStateOf { if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Default.Add }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_home_menu_content_desc),
                    modifier = Modifier.animateIcon({ checkedProgress })
                )
            }
        }
    ) {
        FloatingActionButtonMenuItem(
            onClick = onAddCategoryClick,
            icon = { Icon(Icons.Default.FolderSpecial, contentDescription = null) },
            text = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_home_new_category)) }
        )

        FloatingActionButtonMenuItem(
            onClick = onHomeProjectClick, // Renamed
            icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
            text = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_home_home_category)) }
        )

        FloatingActionButtonMenuItem(
            onClick = onCameraClick,
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
            text = { Text(stringResource(com.zoewave.photodo.model.R.string.applications_photodo_model_route_camera)) }
        )

        if (isAiEnabled) {
            FloatingActionButtonMenuItem(
                onClick = onSmartCaptureClick,
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700)) },
                text = { Text("AI Task") }
            )
        }
    }
}

@Composable
fun HomeOverviewContent(
    uiState: HomeUiState,
    isSearchMode: Boolean,
    onSearchModeChange: (Boolean) -> Unit,
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    onDeleteClicked: (CategoryOverviewUiModel) -> Unit,
    modifier: Modifier = Modifier,
    showSummaryHeader: Boolean = true
) {
    val filteredCategories = remember(uiState.categories, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            uiState.categories
        } else {
            uiState.categories.filter { it.name.contains(uiState.searchQuery, ignoreCase = true) }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.isEmpty) {
            EmptyHomeState(
                onEvent = onEvent,
                navTo = navTo,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2-column dashboard layout
                contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 80.dp), // 🚀 Added bottom padding!
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // --- 🚀 NEW: Header Row (Shortcuts) ---
                item(span = { GridItemSpan(2) }) {
                    AnimatedVisibility(
                        visible = isSearchMode,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { onEvent(HomeEvent.OnSearchQueryChanged(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            placeholder = { Text("Search categories...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    onEvent(HomeEvent.OnSearchQueryChanged(""))
                                    onSearchModeChange(false)
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close search")
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = !isSearchMode,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PhotoDo",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { onSearchModeChange(true) },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    }
                }

                if (showSummaryHeader && !isSearchMode) {
                    item(span = { GridItemSpan(2) }) {
                        OverviewSummaryCard(categories = uiState.categories)
                    }
                }

                itemsIndexed(
                    items = filteredCategories,
                    key = { _, category -> category.id }
                ) { index, category ->
                    CategoryDashboardCard(
                        category = category,
                        index = index,
                        onEvent = onEvent,
                        onDeleteClicked = onDeleteClicked,
                        navTo = navTo
                    )
                }
            }
        }
    }
}

@Composable
fun HomeOverviewDialogs(
    showAddCategorySheet: Boolean,
    onDismissAddCategory: () -> Unit,
    uiState: HomeUiState,
    categoryToDelete: CategoryOverviewUiModel?,
    onDismissDeleteConfirmation: () -> Unit,
    onEvent: (HomeEvent) -> Unit
) {
    if (showAddCategorySheet) {
        AddCategoryBottomSheet(
            onDismiss = onDismissAddCategory,
            onCategoryCreated = { name, iconName ->
                onEvent(HomeEvent.OnAddCategory(name = name, iconUri = iconName))
                onDismissAddCategory()
            }
        )
    }

    if (uiState.isQuickProjectSheetOpen) {
        QuickTemplateBottomSheet(
            onDismiss = { onEvent(HomeEvent.OnDismissBottomSheet) },
            onTemplateSelected = { template ->
                // Ensure it uses the override if present!
                val targetTemplate = if (uiState.quickProjectCategoryOverride != null) {
                    template.copy(categoryName = uiState.quickProjectCategoryOverride)
                } else {
                    template
                }
                onEvent(HomeEvent.OnCreateFromTemplate(targetTemplate))
                onEvent(HomeEvent.OnDismissBottomSheet)
            }
        )
    }

    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = onDismissDeleteConfirmation,
            title = { Text("Delete Category?") },
            text = { Text("This will permanently delete the '${category.name}' category and all its projects. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(HomeEvent.OnDeleteCategory(category.id))
                        onDismissDeleteConfirmation()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteConfirmation) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryBottomSheet(
    onDismiss: () -> Unit,
    onCategoryCreated: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newCategoryName by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.applications_photodo_apps_mobile_features_home_new_category),
                style = MaterialTheme.typography.titleLarge
            )

            // --- QUICK PICK SECTION ---
            Text(
                text = "Quick Pick",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                categoryTemplates.forEach { template ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onCategoryCreated(template.name, template.iconName)
                                },
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = template.icon,
                                    contentDescription = template.name,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newCategoryName,
                onValueChange = { newCategoryName = it },
                label = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_home_category_placeholder)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onCategoryCreated(newCategoryName, "FolderSpecial") },
                enabled = newCategoryName.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.applications_photodo_apps_mobile_features_home_create))
            }
        }
    }
}

private data class CategoryTemplate(val name: String, val icon: ImageVector, val iconName: String)

private val categoryTemplates = listOf(
    CategoryTemplate("Work", Icons.Default.Work, "Work"),
    CategoryTemplate("Personal", Icons.Default.Person, "Person"),
    CategoryTemplate("Home", Icons.Default.Home, "Home"),
    CategoryTemplate("Shopping", Icons.Default.ShoppingCart, "ShoppingCart"),
    CategoryTemplate("Travel", Icons.Default.Flight, "Flight")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDashboardCard(
    category: CategoryOverviewUiModel,
    index: Int,
    onEvent: (HomeEvent) -> Unit,
    onDeleteClicked: (CategoryOverviewUiModel) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamically cycle through Material 3 expressive container colors!
    val (containerColor, contentColor) = when (index % 4) {
        0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        2 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = {
            navTo(PhotoTodoRoute.TasksList(categoryId = category.id, categoryName = category.name))
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Expressive, large rounded corners
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Icon and Percentage Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (category.totalTasks > 0) {
                        Text(
                            text = category.progressText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row {
                    /*IconButton(
                        onClick = { onEvent(HomeEvent.OnAddQuickProjectClicked(category.name)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Project",
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }*/

                    IconButton(
                        onClick = { onDeleteClicked(category) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Category",
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Middle: Category Name & Task Count
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${category.totalProjects} Projects",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Text(
                    text = stringResource(R.string.applications_photodo_apps_mobile_features_home_tasks_count, category.completedTasks, category.totalTasks),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }

            // Bottom: Thicker, expressive progress bar
            if (category.totalTasks > 0) {
                LinearProgressIndicator(
                    progress = { category.progressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)), // Rounded ends for the track
                    color = contentColor, // Use the high-contrast text color for the bar
                    trackColor = contentColor.copy(alpha = 0.2f),
                )
            } else {
                // Placeholder to maintain card height consistency
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun EmptyHomeState(
    onEvent: (HomeEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FolderSpecial,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.applications_photodo_apps_mobile_features_home_welcome),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.applications_photodo_apps_mobile_features_home_empty_state_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, name = "1. Dashboard Populated")
@Composable
private fun HomeOverviewScreenPopulatedPreview() {
    PhotoDoTheme {
        val mockData = listOf(
            CategoryOverviewUiModel(
                id = 1L,
                name = "Real Estate",
                totalProjects = 3,
                totalTasks = 24,
                completedTasks = 18,
                progressPercentage = 0.75f
            ),
            CategoryOverviewUiModel(
                id = 2L,
                name = "Development",
                totalProjects = 5,
                totalTasks = 50,
                completedTasks = 5,
                progressPercentage = 0.10f
            ),
            CategoryOverviewUiModel(
                id = 3L,
                name = "Business",
                totalProjects = 2,
                totalTasks = 12,
                completedTasks = 12,
                progressPercentage = 1.0f
            ),
            CategoryOverviewUiModel(
                id = 4L,
                name = "Personal",
                totalProjects = 4,
                totalTasks = 8,
                completedTasks = 4,
                progressPercentage = 0.50f
            ),
            CategoryOverviewUiModel(
                id = 5L,
                name = "Hobbies",
                totalProjects = 0,
                totalTasks = 0,
                completedTasks = 0,
                progressPercentage = 0.0f
            )
        )

        Surface {
            HomeOverviewScreen(
                uiState = HomeUiState(
                    categories = mockData,
                    urgentProjects = emptyList()
                ),
                onEvent = {},
                navTo = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. Dashboard Empty")
@Composable
private fun HomeOverviewScreenEmptyPreview() {
    PhotoDoTheme {
        Surface {
            HomeOverviewScreen(
                uiState = HomeUiState(),
                onEvent = {},
                navTo = {}
            )
        }
    }
}
