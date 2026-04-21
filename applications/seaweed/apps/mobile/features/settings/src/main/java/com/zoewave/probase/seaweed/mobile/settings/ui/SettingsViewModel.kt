package com.zoewave.probase.seaweed.mobile.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.BudgetTargetRepository
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.data.UserSettingsRepository
import com.zoewave.probase.seaweed.model.BudgetTarget
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig
import com.zoewave.probase.seaweed.model.ThemeMode
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetTargetRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        userSettingsRepository.getUserSettings()
            .onEach { settings ->
                _uiState.value = SettingsUiState.Success(settings)
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SettingsUiEvent) {
        viewModelScope.launch {
            val currentSettings = (uiState.value as? SettingsUiState.Success)?.settings ?: UserSettings()
            when (event) {
                is SettingsUiEvent.UpdateTheme -> {
                    userSettingsRepository.saveUserSettings(currentSettings.copy(themeConfig = event.themeConfig))
                }
                is SettingsUiEvent.UpdateThemeMode -> {
                    userSettingsRepository.saveUserSettings(currentSettings.copy(themeMode = event.themeMode))
                }
                is SettingsUiEvent.UpdateIncome -> {
                    userSettingsRepository.saveUserSettings(currentSettings.copy(monthlyIncome = event.income))
                }
                is SettingsUiEvent.GenerateTestData -> {
                    generateRandomTransactions()
                }
                is SettingsUiEvent.NavigateTo -> { /* Handled in Route */ }
            }
        }
    }

    private suspend fun generateRandomTransactions() {
        val categories = listOf("Food", "Transport", "Shopping", "Entertainment", "Health", "Utilities")
        
        // Generate random budgets first
        categories.forEach { category ->
            val limit = when (category) {
                "Food" -> Random.nextDouble(300.0, 600.0)
                "Transport" -> Random.nextDouble(100.0, 300.0)
                "Shopping" -> Random.nextDouble(200.0, 800.0)
                "Entertainment" -> Random.nextDouble(100.0, 400.0)
                "Health" -> Random.nextDouble(50.0, 200.0)
                "Utilities" -> Random.nextDouble(200.0, 500.0)
                else -> 500.0
            }
            budgetRepository.saveBudget(BudgetTarget(category, limit))
        }

        val now = System.currentTimeMillis()
        val ninetyDaysMillis = 90L * 24 * 60 * 60 * 1000

        repeat(150) {
            val randomTimeOffset = Random.nextLong(0, ninetyDaysMillis)
            val date = now - randomTimeOffset
            val category = categories.random()
            val amount = when (category) {
                "Food" -> Random.nextDouble(5.0, 50.0)
                "Transport" -> Random.nextDouble(2.0, 30.0)
                "Shopping" -> Random.nextDouble(10.0, 200.0)
                "Entertainment" -> Random.nextDouble(10.0, 100.0)
                "Health" -> Random.nextDouble(20.0, 150.0)
                "Utilities" -> Random.nextDouble(50.0, 300.0)
                else -> Random.nextDouble(1.0, 100.0)
            }

            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                category = category,
                description = "Random $category expense",
                date = date
            )
            transactionRepository.addTransaction(transaction)
        }
    }
}

sealed interface SettingsUiState {
    object Loading : SettingsUiState
    data class Success(val settings: UserSettings) : SettingsUiState
}

sealed interface SettingsUiEvent {
    data class UpdateTheme(val themeConfig: SeaweedThemeConfig) : SettingsUiEvent
    data class UpdateThemeMode(val themeMode: ThemeMode) : SettingsUiEvent
    data class UpdateIncome(val income: Double) : SettingsUiEvent
    object GenerateTestData : SettingsUiEvent
    data class NavigateTo(val destination: com.zoewave.probase.seaweed.model.navigation.SeaweedDestination) : SettingsUiEvent
}
