package com.zoewave.probase.seaweed.features.spendingcontrol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.CategoryRepository
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.Envelope
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.EnvelopePriority
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.EnvelopeRepository
import com.zoewave.probase.seaweed.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EnvelopeUiState(
    val envelopes: List<Envelope> = emptyList(),
    val availableCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface EnvelopeUiEvent {
    data class AddEnvelope(
        val name: String,
        val limitCents: Long,
        val categoryIds: List<String>
    ) : EnvelopeUiEvent
    data class DeleteEnvelope(val id: String) : EnvelopeUiEvent
}

@HiltViewModel
class EnvelopeViewModel @Inject constructor(
    private val repository: EnvelopeRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<EnvelopeUiState> = combine(
        repository.getAllEnvelopes(),
        categoryRepository.getAllCategories()
    ) { envelopes, categories ->
        EnvelopeUiState(
            envelopes = envelopes,
            availableCategories = categories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EnvelopeUiState(isLoading = true)
    )

    fun onEvent(event: EnvelopeUiEvent) {
        viewModelScope.launch {
            when (event) {
                is EnvelopeUiEvent.AddEnvelope -> {
                    repository.saveEnvelope(
                        Envelope(
                            id = UUID.randomUUID().toString(),
                            name = event.name,
                            monthlyLimitCents = event.limitCents,
                            currentSpentCents = 0,
                            categoryIds = event.categoryIds,
                            priority = EnvelopePriority.NORMAL
                        )
                    )
                }
                is EnvelopeUiEvent.DeleteEnvelope -> {
                    // TODO: Implement delete in repository
                }
            }
        }
    }
}
