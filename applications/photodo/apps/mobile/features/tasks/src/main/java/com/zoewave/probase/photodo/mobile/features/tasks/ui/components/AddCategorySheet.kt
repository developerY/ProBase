package com.zoewave.probase.photodo.mobile.features.tasks.ui.components



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TaskDraftState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategorySheet(
    draftState: TaskDraftState,
    onEvent: (TasksEvent) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
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
                value = draftState.newCategoryName,
                onValueChange = { /* You'll need to add OnDraftCategoryNameChanged to your events! */ },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onEvent(TasksEvent.OnSaveDraftClicked) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Create Category")
            }
        }
    }
}