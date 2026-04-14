package com.zoewave.probase.photodo.features.smartadvice.ui

import com.zoewave.probase.photodo.features.smartadvice.domain.ProjectAdvice

sealed interface SmartAdviceUiState {
    data object Idle : SmartAdviceUiState
    data object Loading : SmartAdviceUiState
    data class Success(
        val advice: ProjectAdvice,
        val chatHistory: List<ChatMessage> = emptyList(),
        val isSendingQuestion: Boolean = false
    ) : SmartAdviceUiState
    data class Error(val message: String) : SmartAdviceUiState
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)
