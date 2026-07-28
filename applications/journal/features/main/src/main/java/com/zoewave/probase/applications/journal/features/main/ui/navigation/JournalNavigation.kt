package com.zoewave.probase.applications.journal.features.main.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.applications.journal.features.main.ui.AddEditJournalScreen
import com.zoewave.probase.applications.journal.features.main.ui.JournalFullView
import com.zoewave.probase.applications.journal.features.main.ui.JournalViewModel
import com.zoewave.probase.features.camera.ui.CameraUIRoute

sealed class JournalScreen {
    data object List : JournalScreen()
    data object AddEdit : JournalScreen()
    data object Camera : JournalScreen()
}

/**
 * A self-contained Journal Navigator that apps can drop into their UI.
 */
@Composable
fun JournalNavHost(
    onExit: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<JournalScreen>(JournalScreen.List) }
    val viewModel: JournalViewModel = hiltViewModel()

    when (currentScreen) {
        JournalScreen.List -> {
            JournalFullView(
                viewModel = viewModel,
                onAddEntry = {
                    viewModel.onEntrySelected(null)
                    currentScreen = JournalScreen.AddEdit
                },
                onEditEntry = { entry ->
                    viewModel.onEntrySelected(entry)
                    currentScreen = JournalScreen.AddEdit
                }
            )
        }
        JournalScreen.AddEdit -> {
            AddEditJournalScreen(
                viewModel = viewModel,
                onBack = { currentScreen = JournalScreen.List },
                onTakePhoto = { currentScreen = JournalScreen.Camera }
            )
        }
        JournalScreen.Camera -> {
            CameraUIRoute(
                navTo = { result ->
                    if (result.startsWith("result_ok:")) {
                        val uriString = result.substringAfter("result_ok:")
                        viewModel.addImages(listOf(Uri.parse(uriString)))
                    }
                    currentScreen = JournalScreen.AddEdit
                }
            )
        }
    }
}
