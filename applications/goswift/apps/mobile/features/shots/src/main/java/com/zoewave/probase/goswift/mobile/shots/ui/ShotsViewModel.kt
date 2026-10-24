package com.zoewave.probase.goswift.mobile.shots.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.goswift.data.ShotRepository
import com.zoewave.probase.goswift.model.CaffeineShot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ShotsViewModel @Inject constructor(
    private val repository: ShotRepository
) : ViewModel() {

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault())

    val uiState: StateFlow<ShotsUiState> = repository.getAllShots()
        .map { shots ->
            val timelineItems = if (shots.isNotEmpty()) {
                shots.mapIndexed { index, shot ->
                    TimelineItemData(
                        id = shot.id,
                        time = timeFormatter.format(Instant.ofEpochMilli(shot.timestamp)),
                        title = "Microdose #${shots.size - index}",
                        subtitle = "${shot.mg}mg Fuel Ingestion",
                        amountMg = shot.mg,
                        status = "Logged",
                        isCompleted = true
                    )
                }
            } else {
                listOf(
                    TimelineItemData(
                        id = "default_1",
                        time = "08:30 AM",
                        title = "Morning Activation",
                        subtitle = "Espresso • Focus Booster",
                        amountMg = 35,
                        status = "Logged",
                        isCompleted = true
                    ),
                    TimelineItemData(
                        id = "default_2",
                        time = "11:15 AM",
                        title = "Deep Work Sprint",
                        subtitle = "Green Tea • Microdose",
                        amountMg = 20,
                        status = "Logged",
                        isCompleted = true
                    ),
                    TimelineItemData(
                        id = "default_3",
                        time = "02:15 PM",
                        title = "Afternoon Focus Window",
                        subtitle = "AI Recommended • Paired with 250ml water",
                        amountMg = 18,
                        status = "Scheduled",
                        isCompleted = false,
                        isNextWindow = true
                    )
                )
            }
            val totalLogged = timelineItems.filter { it.isCompleted }.sumOf { it.amountMg }
            val totalIntended = timelineItems.sumOf { it.amountMg }

            ShotsUiState.Success(
                shots = shots,
                timelineItems = timelineItems,
                intendedActiveDosingMg = totalIntended,
                loggedMg = totalLogged,
                safeCeilingMg = 120
            ) as ShotsUiState
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ShotsUiState.Loading
        )

    fun onEvent(event: ShotsUiEvent) {
        when (event) {
            is ShotsUiEvent.DeleteShot -> {
                viewModelScope.launch {
                    repository.deleteShot(event.id)
                }
            }
        }
    }
}
