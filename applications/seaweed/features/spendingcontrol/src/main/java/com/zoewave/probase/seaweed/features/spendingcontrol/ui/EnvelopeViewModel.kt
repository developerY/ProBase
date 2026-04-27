package com.zoewave.probase.seaweed.features.spendingcontrol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.Envelope
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.EnvelopeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EnvelopeViewModel @Inject constructor(
    private val repository: EnvelopeRepository
) : ViewModel() {

    val envelopes: StateFlow<List<Envelope>> = repository.getAllEnvelopes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
