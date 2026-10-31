package com.zoewave.probase.features.ai.local.data

sealed interface LocalModelState {
    data object NotDownloaded : LocalModelState
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long, val progressPercent: Int) : LocalModelState
    data object VerifyingChecksum : LocalModelState
    data class Ready(val absoluteModelPath: String, val modelVersion: String) : LocalModelState
    data class Error(val reason: String, val isRetryable: Boolean) : LocalModelState
}
