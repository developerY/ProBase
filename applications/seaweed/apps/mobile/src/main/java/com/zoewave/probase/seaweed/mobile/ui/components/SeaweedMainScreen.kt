package com.zoewave.probase.seaweed.mobile.ui.components

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import android.widget.Toast
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass.Companion.calculateFromSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.seaweed.mobile.glass.GlassesActivity
import com.zoewave.probase.seaweed.mobile.ui.navigation.seaweedNavEntryProvider
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalProjectedApi::class)
@Composable
fun SeaweedMainScreen(
    windowSizeClass: WindowSizeClass
) {
    val context = LocalContext.current
    val isProjectedConnected by remember(context) {
        if (Build.VERSION.SDK_INT >= 36) {
            ProjectedContext.isProjectedDeviceConnected(context, Dispatchers.Main)
        } else {
            flowOf(false)
        }
    }.collectAsStateWithLifecycle(initialValue = false)

    val backStack = remember {
        mutableStateListOf<SeaweedDestination>(SeaweedDestination.Home)
    }

    val currentDestination = backStack.lastOrNull() ?: SeaweedDestination.Home

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            SeaweedBottomBar(
                currentDestination = currentDestination,
                navTo = { selectedDestination ->
                    if (currentDestination != selectedDestination) {
                        backStack.clear()
                        backStack.add(SeaweedDestination.Home)
                        if (selectedDestination != SeaweedDestination.Home) {
                            if (selectedDestination is SeaweedDestination.Transactions) {
                                backStack.add(SeaweedDestination.Transactions())
                            } else {
                                backStack.add(selectedDestination)
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = { key ->
                seaweedNavEntryProvider(
                    key = key,
                    windowSizeClass = windowSizeClass,
                    navigateTo = { dest ->
                        if (dest != backStack.lastOrNull()) {
                            backStack.add(dest)
                        }
                    },
                    onBack = { backStack.removeLastOrNull() },
                    topBarActions = {
                        if (Build.VERSION.SDK_INT >= 35) {
                            IconButton(
                                onClick = {
                                    try {
                                        val options = ProjectedContext.createProjectedActivityOptions(context)
                                        val intent = Intent(context, GlassesActivity::class.java)
                                        context.startActivity(intent, options.toBundle())
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Glasses not connected", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = isProjectedConnected || Build.VERSION.SDK_INT < 36
                            ) {
                                Icon(
                                    Icons.Default.Cast,
                                    contentDescription = "Launch on Glasses",
                                    tint = if (isProjectedConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun SeaweedMainScreenPreview() {
    SeaweedMainScreen(
        windowSizeClass = calculateFromSize(DpSize(400.dp, 800.dp))
    )
}
