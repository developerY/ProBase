package com.zoewave.probase.kocolor.mobile.ui

import androidx.lifecycle.ViewModel
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiscoveryStatusViewModel @Inject constructor(
    private val sessionRepository: FashionSessionRepository
) : ViewModel() {
    val discoveryStatus = sessionRepository.discoveryStatus
}
