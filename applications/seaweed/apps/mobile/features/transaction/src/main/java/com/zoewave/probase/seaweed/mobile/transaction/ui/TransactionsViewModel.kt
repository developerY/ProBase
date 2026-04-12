package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.model.navigation.TransactionTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectedTransactionId = MutableStateFlow<String?>(null)
    private val _selectedTab = MutableStateFlow(TransactionTab.RECENT)
    private var isInitialized = false

    fun setInitialCategory(category: String?) {
        if (!isInitialized && category != null) {
            _selectedCategory.value = category
            isInitialized = true
        }
    }

    fun setInitialTab(tab: TransactionTab) {
        if (!isInitialized) {
            _selectedTab.value = tab
            isInitialized = true
        }
    }

    private val _selectedTransaction = _selectedTransactionId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getTransaction(id)
    }

    val uiState: StateFlow<TransactionsUiState> = combine(
        repository.getAllTransactions(),
        _selectedCategory,
        _selectedTransactionId,
        _selectedTransaction,
        _selectedTab
    ) { transactions, selectedCategory, selectedTransactionId, selectedTransaction, selectedTab ->
        val categories = transactions.map { it.category }.distinct().sorted()
        val filteredTransactions = if (selectedCategory == null) {
            transactions
        } else {
            transactions.filter { it.category == selectedCategory }
        }
        TransactionsUiState.Success(
            transactions = transactions,
            filteredTransactions = filteredTransactions,
            categories = categories,
            selectedCategory = selectedCategory,
            selectedTransactionId = selectedTransactionId,
            selectedTransaction = selectedTransaction,
            selectedTab = selectedTab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsUiState.Loading
    )

    fun onEvent(event: TransactionsUiEvent) {
        when (event) {
            is TransactionsUiEvent.DeleteTransaction -> {
                viewModelScope.launch {
                    repository.deleteTransaction(event.id)
                }
            }
            is TransactionsUiEvent.SelectCategory -> {
                _selectedCategory.value = event.category
            }
            is TransactionsUiEvent.SelectTransaction -> {
                _selectedTransactionId.value = event.id
            }
            is TransactionsUiEvent.SelectTab -> {
                _selectedTab.value = event.tab
            }
        }
    }
}
