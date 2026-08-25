package com.zoewave.probase.kocolor.features.analyzer.playlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.PlaylistRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.usecase.GeneratePlaylistUseCase
import com.zoewave.probase.kocolor.db.entity.DailyStylePlanEntity
import com.zoewave.probase.kocolor.db.entity.PlaylistWithDays
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.ResultTab
import com.zoewave.probase.kocolor.model.playlist.PlaylistStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ResolvedDailyPlan(
    val plan: DailyStylePlanEntity,
    val clothingItems: List<ClothingItem>,
    val cosmeticItems: List<CosmeticItem>
)

data class StylePlaylistUiState(
    val currentPlaylist: List<ResolvedDailyPlan> = emptyList(),
    val selectedPlanForDetail: ResolvedDailyPlan? = null,
    val selectedResultTab: ResultTab = ResultTab.CLOTHES,
    val playlistStatus: PlaylistStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface StylePlaylistEvent {
    data object GenerateWeekly : StylePlaylistEvent
    data class CommitDay(val planId: String, val productIds: List<String>) : StylePlaylistEvent
    data class SelectPlanForDetail(val plan: ResolvedDailyPlan?) : StylePlaylistEvent
    data class SelectResultTab(val tab: ResultTab) : StylePlaylistEvent
}

@HiltViewModel
class StylePlaylistViewModel @Inject constructor(
    private val generatePlaylistUseCase: GeneratePlaylistUseCase,
    private val playlistRepository: PlaylistRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _selectedPlan = MutableStateFlow<ResolvedDailyPlan?>(null)
    private val _selectedResultTab = MutableStateFlow(ResultTab.CLOTHES)

    val uiState: StateFlow<StylePlaylistUiState> = combine(
        playlistRepository.observeLatestPlaylist(),
        wardrobeRepository.getAllClothing(),
        cosmeticRepository.getAllCosmetics(),
        _isLoading,
        _error,
        _selectedPlan,
        _selectedResultTab
    ) { array ->
        val playlist = array[0] as PlaylistWithDays?
        val wardrobe = array[1] as List<ClothingItem>
        val cosmetics = array[2] as List<CosmeticItem>
        val loading = array[3] as Boolean
        val err = array[4] as String?
        val selected = array[5] as ResolvedDailyPlan?
        val tab = array[6] as ResultTab

        val resolvedPlans = playlist?.dailyPlans?.map { plan ->
            val clothingIds = plan.baseOutfitProductIds.mapNotNull { it.removePrefix("w_").toLongOrNull() }
            val cosmeticIds = plan.cosmeticProductIds.mapNotNull { it.removePrefix("c_").toLongOrNull() }
            
            ResolvedDailyPlan(
                plan = plan,
                clothingItems = wardrobe.filter { it.internalId in clothingIds },
                cosmeticItems = cosmetics.filter { it.internalId in cosmeticIds }
            )
        } ?: emptyList()

        StylePlaylistUiState(
            currentPlaylist = resolvedPlans.sortedBy { it.plan.targetDate },
            selectedPlanForDetail = selected,
            selectedResultTab = tab,
            playlistStatus = playlist?.playlist?.status,
            isLoading = loading,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StylePlaylistUiState(isLoading = true)
    )

    fun onEvent(event: StylePlaylistEvent) {
        when (event) {
            StylePlaylistEvent.GenerateWeekly -> generate()
            is StylePlaylistEvent.CommitDay -> commit(event.planId, event.productIds)
            is StylePlaylistEvent.SelectPlanForDetail -> _selectedPlan.value = event.plan
            is StylePlaylistEvent.SelectResultTab -> _selectedResultTab.value = event.tab
        }
    }

    private fun generate() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = generatePlaylistUseCase.generateWeeklyPlaylist(LocalDate.now())
            result.onSuccess {
                _isLoading.value = false
            }.onFailure { e ->
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }

    private fun commit(planId: String, productIds: List<String>) {
        viewModelScope.launch {
            playlistRepository.commitDailyOutfit(planId, productIds)
        }
    }
}
