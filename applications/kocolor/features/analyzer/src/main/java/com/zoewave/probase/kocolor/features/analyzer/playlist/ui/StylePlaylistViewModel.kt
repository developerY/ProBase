package com.zoewave.probase.kocolor.features.analyzer.playlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.repository.PlaylistRepository
import com.zoewave.probase.kocolor.data.usecase.GeneratePlaylistUseCase
import com.zoewave.probase.kocolor.db.entity.PlaylistWithDays
import com.zoewave.probase.kocolor.model.playlist.PlaylistStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StylePlaylistUiState(
    val currentPlaylist: PlaylistWithDays? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface StylePlaylistEvent {
    data object GenerateWeekly : StylePlaylistEvent
    data class CommitDay(val planId: String, val productIds: List<String>) : StylePlaylistEvent
}

@HiltViewModel
class StylePlaylistViewModel @Inject constructor(
    private val generatePlaylistUseCase: GeneratePlaylistUseCase,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StylePlaylistUiState())
    val uiState: StateFlow<StylePlaylistUiState> = _uiState.asStateFlow()

    // For simplicity in V1, we track the "active" playlist ID in memory or fetch latest
    // In a real app, this would be persisted in settings or similar
    private val activePlaylistId = MutableStateFlow<String?>(null)

    init {
        playlistRepository.observeLatestPlaylist()
            .onEach { playlist ->
                _uiState.update { it.copy(currentPlaylist = playlist, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: StylePlaylistEvent) {
        when (event) {
            StylePlaylistEvent.GenerateWeekly -> generate()
            is StylePlaylistEvent.CommitDay -> commit(event.planId, event.productIds)
        }
    }

    private fun generate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = generatePlaylistUseCase.generateWeeklyPlaylist(LocalDate.now())
            result.onSuccess { id ->
                activePlaylistId.value = id
                observePlaylist(id)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun observePlaylist(id: String) {
        playlistRepository.observePlaylist(id)
            .onEach { playlist ->
                _uiState.update { it.copy(currentPlaylist = playlist, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun commit(planId: String, productIds: List<String>) {
        viewModelScope.launch {
            playlistRepository.commitDailyOutfit(planId, productIds)
        }
    }
}
