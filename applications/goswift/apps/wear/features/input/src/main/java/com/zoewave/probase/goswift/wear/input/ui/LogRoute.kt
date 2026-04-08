package com.zoewave.probase.goswift.wear.input.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material3.*
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.rememberColumnState
import com.zoewave.probase.goswift.wear.hydration.ui.HydrationUiEvent
import com.zoewave.probase.goswift.wear.hydration.ui.HydrationViewModel
import com.zoewave.probase.goswift.wear.input.ui.NutritionUiEvent
import com.zoewave.probase.goswift.wear.input.ui.NutritionViewModel
import com.zoewave.probase.goswift.wear.shots.ui.AddShotUiEvent
import com.zoewave.probase.goswift.wear.shots.ui.AddShotViewModel

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LogRoute(
    modifier: Modifier = Modifier,
    shotsViewModel: AddShotViewModel = hiltViewModel(),
    hydrationViewModel: HydrationViewModel = hiltViewModel(),
    nutritionViewModel: NutritionViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val columnState = rememberColumnState()

    ScalingLazyColumn(
        columnState = columnState,
        modifier = modifier.fillMaxSize()
    ) {
        item {
            ListHeader {
                Text("Log Activity")
            }
        }

        // Caffeine Section
        item {
            Text("Caffeine (mg)", style = MaterialTheme.typography.labelMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { shotsViewModel.onEvent(AddShotUiEvent.MgChanged("20")); shotsViewModel.onEvent(AddShotUiEvent.SaveShot); onBack() }) {
                    Text("20")
                }
                Button(onClick = { shotsViewModel.onEvent(AddShotUiEvent.MgChanged("40")); shotsViewModel.onEvent(AddShotUiEvent.SaveShot); onBack() }) {
                    Text("40")
                }
                Button(onClick = { shotsViewModel.onEvent(AddShotUiEvent.MgChanged("80")); shotsViewModel.onEvent(AddShotUiEvent.SaveShot); onBack() }) {
                    Text("80")
                }
            }
        }

        // Water Section
        item {
            Text("Water (ml)", style = MaterialTheme.typography.labelMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { hydrationViewModel.onEvent(HydrationUiEvent.AddWater(0.25)); onBack() }) {
                    Text("250")
                }
                Button(onClick = { hydrationViewModel.onEvent(HydrationUiEvent.AddWater(0.5)); onBack() }) {
                    Text("500")
                }
            }
        }

        // Calories Section
        item {
            Text("Calories (kcal)", style = MaterialTheme.typography.labelMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { nutritionViewModel.onEvent(NutritionUiEvent.AddMeal("Quick Snack", 200.0)); onBack() }) {
                    Text("200")
                }
                Button(onClick = { nutritionViewModel.onEvent(NutritionUiEvent.AddMeal("Quick Meal", 500.0)); onBack() }) {
                    Text("500")
                }
            }
        }
        
        item {
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }
}
