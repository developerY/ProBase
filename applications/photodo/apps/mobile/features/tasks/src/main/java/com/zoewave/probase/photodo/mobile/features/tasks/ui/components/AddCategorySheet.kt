package com.zoewave.probase.photodo.mobile.features.tasks.ui.components



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TaskDraftState


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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp), // Extra padding for system nav bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("New Category", style = MaterialTheme.typography.titleLarge)

            // Because this modifies the draftState in the ViewModel,
            // typing here survives rotation!
            OutlinedTextField(
                value = uiState.newCategoryName,
                onValueChange = { onEvent(TasksEvent.OnDraftCategoryNameChanged(it)) },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onEvent(TasksEvent.OnSaveDraftClicked) },
                enabled = uiState.newCategoryName.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Create Category")
            }
        }
    }
}