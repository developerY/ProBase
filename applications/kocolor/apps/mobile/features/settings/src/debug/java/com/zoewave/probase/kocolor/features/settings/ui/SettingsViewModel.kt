package com.zoewave.probase.kocolor.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.features.settings.domain.seeder.VaultSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SeedingState {
    data object Idle : SeedingState()
    data object Loading : SeedingState()
    data object Success : SeedingState()
    data class Error(val message: String) : SeedingState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val vaultSeeder: VaultSeeder
) : ViewModel() {

    private val _seedingState = MutableStateFlow<SeedingState>(SeedingState.Idle)
    val seedingState: StateFlow<SeedingState> = _seedingState.asStateFlow()

    fun triggerDatabaseSeed() {
        viewModelScope.launch {
            _seedingState.value = SeedingState.Loading
            vaultSeeder.wipeAndSeedDatabase()
                .onSuccess {
                    _seedingState.value = SeedingState.Success
                }
                .onFailure { error ->
                    _seedingState.value = SeedingState.Error(error.localizedMessage ?: "Unknown Error")
                }
        }
    }
}
