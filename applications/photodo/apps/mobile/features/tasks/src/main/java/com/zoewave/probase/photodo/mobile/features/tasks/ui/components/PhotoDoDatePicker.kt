package com.zoewave.probase.photodo.mobile.features.tasks.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.R
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDoDatePicker(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    // Initializes the native Material 3 calendar state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    // Return the selected timestamp, or null if nothing was picked
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_set_date_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_cancel_button))
            }
        }
    ) {
        // The actual calendar UI
        DatePicker(state = datePickerState)
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoDoDatePickerPreview() {
    PhotoDoTheme {
        PhotoDoDatePicker(
            onDateSelected = {},
            onDismiss = {}
        )
    }
}
