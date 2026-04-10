package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.R
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TaskDraftState
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategorySheet(
    uiState: TaskDraftState,
    onEvent: (TasksEvent) -> Unit,
    navTo: (PhotoTodoRoute?) -> Unit, // ✅ Standardized Navigation Channel
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(TasksEvent.OnDismissBottomSheet) },
        sheetState = sheetState,
        modifier = modifier
    ) {
        AddCategorySheetContent(
            newCategoryName = uiState.newCategoryName,
            onDraftCategoryNameChanged = { onEvent(TasksEvent.OnDraftCategoryNameChanged(it)) },
            onSaveDraftClicked = { onEvent(TasksEvent.OnSaveDraftClicked) }
        )
    }
}

@Composable
fun AddCategorySheetContent(
    newCategoryName: String,
    onDraftCategoryNameChanged: (String) -> Unit,
    onSaveDraftClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 32.dp), // Extra padding for system nav bar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_new_category),
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
                QuickCategoryIcon(
                    template = template,
                    onClick = {
                        onDraftCategoryNameChanged(template.name)
                        onSaveDraftClicked()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newCategoryName,
            onValueChange = onDraftCategoryNameChanged,
            label = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_category_name_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSaveDraftClicked,
            enabled = newCategoryName.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_create_category_button))
        }
    }
}

@Composable
private fun QuickCategoryIcon(
    template: CategoryTemplate,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onClick() },
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

private data class CategoryTemplate(val name: String, val icon: ImageVector)

private val categoryTemplates = listOf(
    CategoryTemplate("Work", Icons.Default.Work),
    CategoryTemplate("Personal", Icons.Default.Person),
    CategoryTemplate("Home", Icons.Default.Home),
    CategoryTemplate("Shopping", Icons.Default.ShoppingCart),
    CategoryTemplate("Travel", Icons.Default.Flight)
)

@Preview(showBackground = true)
@Composable
private fun AddCategorySheetPreview() {
    PhotoDoTheme {
        AddCategorySheetContent(
            newCategoryName = "Work",
            onDraftCategoryNameChanged = {},
            onSaveDraftClicked = {}
        )
    }
}