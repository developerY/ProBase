package com.zoewave.probase.kocolor.features.routines.ui

import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.features.xr.glass.GlassesMainActivity
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalProjectedApi::class)
@Composable
fun RoutineDetailUiRoute(
    routineId: Long,
    onNavigateTo: (KoColorRoute) -> Unit,
    viewModel: RoutinesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            ProjectedContext.isProjectedDeviceConnected(context, this.coroutineContext)
                .collectLatest { isConnected ->
                    viewModel.updateGlassConnection(isConnected)
                }
        } else {
            viewModel.updateGlassConnection(false)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is RoutinesSideEffect.LaunchGlassProjection -> {
                    if (Build.VERSION.SDK_INT >= 35) {
                        try {
                            val options = ProjectedContext.createProjectedActivityOptions(context)
                            val intent = Intent(context, GlassesMainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra("initial_sample", "Ritual")
                                putExtra("routine_time", effect.time.name)
                            }
                            context.startActivity(intent, options.toBundle())
                        } catch (e: Exception) {
                            Log.e("RoutineDetailUiRoute", "Projection Launch Failed", e)
                            Toast.makeText(context, "Projection Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        try {
                            val intent = Intent(context, GlassesMainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra("initial_sample", "Ritual")
                                putExtra("routine_time", effect.time.name)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("RoutineDetailUiRoute", "Launch Failed", e)
                        }
                    }
                }
            }
        }
    }

    RoutineDetailScreen(
        uiState = RoutineDetailUiState(routineId, uiState),
        onEvent = viewModel::onEvent,
        navTo = onNavigateTo
    )
}
