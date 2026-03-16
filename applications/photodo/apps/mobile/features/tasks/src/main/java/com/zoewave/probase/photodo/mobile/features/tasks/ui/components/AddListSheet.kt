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
fun AddListSheet(
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
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Create Task List", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = draftState.listTitle,
                onValueChange = { onEvent(TasksEvent.OnDraftTitleChanged(it)) },
                label = { Text("List Title (e.g. PreFab Home Setup)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onEvent(TasksEvent.OnSaveDraftClicked) },
                modifier = Modifier.align(Alignment.End),
                enabled = draftState.listTitle.isNotBlank() // Quick validation
            ) {
                Text("Save List")
            }
        }
    }
}