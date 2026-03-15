package com.zoewave.probase.photodo.mobile.ui.navigation


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.zoewave.probase.photodo.mobile.features.home.ui.HomeUiRoute
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksListUiRoute

//import com.zoewave.probase.photodo.features.home.ui.HomeScreen
//import com.zoewave.probase.photodo.features.settings.ui.SettingsScreen
//import com.zoewave.probase.photodo.features.tasks.ui.TasksListScreen

fun photoTodoNavEntryProvider(
    key: PhotoTodoRoute,
    navigateTo: (PhotoTodoRoute) -> Unit
): NavEntry<PhotoTodoRoute> {

    // NavEntry wraps the content and scopes Hilt ViewModels to this specific backstack entry
    return NavEntry(key) {
        when (key) {
            is PhotoTodoRoute.Home -> {
                // Example of scoping a ViewModel to the Home route
                // val viewModel: HomeViewModel = hiltViewModel()
                HomeUiRoute(
                    modifier = Modifier.fillMaxSize(),
                    // Pass navTo if the screen needs to trigger deeper navigation
                    // navTo = navigateTo
                )
            }

            is PhotoTodoRoute.TasksList -> {
                // val viewModel: TasksViewModel = hiltViewModel()
                TasksListUiRoute()
            }

            is PhotoTodoRoute.Settings -> {
                // val viewModel: SettingsViewModel = hiltViewModel()
                Text("Settings")
                /*SettingsScreen(
                    modifier = Modifier.fillMaxSize()
                )*/
            }
        }
    }
}