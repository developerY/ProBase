package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import com.zoewave.probase.kocolor.data.FashionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val fashionRepository: FashionRepository
) : ViewModel() {

    private val _analysis = MutableStateFlow<SavedAnalysis?>(null)
    val analysis: StateFlow<SavedAnalysis?> = _analysis.asStateFlow()

    fun loadAnalysis(id: Long) {
        viewModelScope.launch {
            _analysis.value = fashionRepository.getSuggestionById(id)
        }
    }
}
