package com.zoewave.probase.photodo.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass.Companion.calculateFromSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.ui.navigation.photoTodoNavEntryProvider
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute

@Composable
fun PhotoDoMainScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: PhotoDoMainViewModel = hiltViewModel(),
    entryProvider: (PhotoTodoRoute, WindowSizeClass, Boolean, () -> Unit, (PhotoTodoRoute) -> Unit) -> NavEntry<PhotoTodoRoute> = { key, size, ai, back, to ->
        photoTodoNavEntryProvider(key, size, ai, to, back)
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PhotoTodoBottomBar(
                currentRoute = uiState.currentRoute,
                navTo = { selectedRoute ->
                    viewModel.onEvent(PhotoDoMainEvent.OnNavigateTo(selectedRoute))
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = uiState.backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = { 
                viewModel.onEvent(PhotoDoMainEvent.OnNavigateBack)
            },
            entryProvider = { key ->
                // ✅ DELEGATE: Call the provider function
                entryProvider(
                    key,
                    windowSizeClass,
                    uiState.isAiEnabled,
                    { 
                        viewModel.onEvent(PhotoDoMainEvent.OnNavigateBack)
                    },
                    { dest ->
                        viewModel.onEvent(PhotoDoMainEvent.OnNavigateTo(dest))
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun PhotoDoMainScreenPreview() {
    val configuration = LocalConfiguration.current
    @Suppress("ConfigurationScreenWidthHeight")
    val windowSizeClass = calculateFromSize(DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp))
    PhotoDoTheme {
        PhotoDoMainScreen(
            windowSizeClass = windowSizeClass,
            entryProvider = { key, _, _, _, _ ->
                NavEntry(key) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Mock Screen: ${key::class.simpleName}")
                    }
                }
            }
        )
    }
}
