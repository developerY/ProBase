package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_new_category), style = MaterialTheme.typography.titleLarge)

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