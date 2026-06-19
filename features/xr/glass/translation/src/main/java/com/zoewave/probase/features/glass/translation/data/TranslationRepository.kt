package com.zoewave.probase.features.glass.translation.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepository @Inject constructor() {
    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    fun updateTranscription(text: String) {
        _transcribedText.value = text
    }

    fun updateTranslation(text: String) {
        _translatedText.value = text
    }

    fun updateListening(listening: Boolean) {
        _isListening.value = listening
    }

    fun updateTranslating(translating: Boolean) {
        _isTranslating.value = translating
    }

    fun clear() {
        _transcribedText.value = ""
        _translatedText.value = ""
        _isListening.value = false
        _isTranslating.value = false
    }
}
