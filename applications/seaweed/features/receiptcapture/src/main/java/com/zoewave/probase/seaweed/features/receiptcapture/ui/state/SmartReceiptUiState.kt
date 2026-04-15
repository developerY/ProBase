package com.zoewave.probase.seaweed.features.receiptcapture.ui.state

import androidx.compose.runtime.Immutable
import com.zoewave.probase.seaweed.features.receiptcapture.domain.SmartReceiptDraft

@Immutable
sealed interface SmartReceiptUiState {
    data class Idle(
        val capturedUri: String? = null,
        val userComment: String = ""
    ) : SmartReceiptUiState
    
    data class Loading(
        val logs: List<String> = emptyList(),
        val isUsingCloud: Boolean = false,
        val networkSpeed: String? = null
    ) : SmartReceiptUiState
    
    data class Success(
        val draft: SmartReceiptDraft,
        val engineUsed: String,
        val diagnostics: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    ) : SmartReceiptUiState
    
    data class Error(
        val message: String,
        val diagnostics: List<String> = emptyList()
    ) : SmartReceiptUiState
}
