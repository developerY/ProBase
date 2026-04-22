package com.zoewave.probase.features.health.cgm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.health.cgm.data.di.GlucoseRepositoryFactory
import com.zoewave.probase.features.health.cgm.data.repository.GlucoseRepository
import com.zoewave.probase.core.model.health.GlucoseReading
import com.zoewave.probase.core.model.health.GlucoseSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlucoseViewModel @Inject constructor(
    private val repositoryFactory: GlucoseRepositoryFactory
) : ViewModel() {

    private val _selectedSource = MutableStateFlow(GlucoseSource.SIMULATOR)
    val selectedSource: StateFlow<GlucoseSource> = _selectedSource.asStateFlow()

    private val _latestReading = MutableStateFlow<GlucoseReading?>(null)
    val latestReading: StateFlow<GlucoseReading?> = _latestReading.asStateFlow()

    private var collectionJob: Job? = null
    private var currentRepository: GlucoseRepository? = null

    init {
        switchSource(GlucoseSource.SIMULATOR)
    }

    fun switchSource(source: GlucoseSource) {
        _selectedSource.value = source
        collectionJob?.cancel()
        
        currentRepository = repositoryFactory.create(source)
        
        collectionJob = viewModelScope.launch {
            currentRepository?.glucoseReadings?.collect { reading ->
                _latestReading.value = reading
            }
        }
    }

    fun triggerScan() {
        viewModelScope.launch {
            currentRepository?.scanSensor()
        }
    }
}
