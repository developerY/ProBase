package com.zoewave.probase.applications.journal.features.main.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.journal.data.JournalRepository
import com.zoewave.probase.applications.journal.model.JournalEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository
) : ViewModel() {

    val journalEntries: StateFlow<List<JournalEntry>> = repository.getJournalEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentEntry = MutableStateFlow<JournalEntry?>(null)
    val currentEntry = _currentEntry.asStateFlow()

    // Hoisted draft state to survive navigation to Camera
    private val _draftTitle = MutableStateFlow("")
    val draftTitle = _draftTitle.asStateFlow()

    private val _draftContent = MutableStateFlow("")
    val draftContent = _draftContent.asStateFlow()

    private val _draftImages = MutableStateFlow<List<Uri>>(emptyList())
    val draftImages = _draftImages.asStateFlow()

    fun onEntrySelected(entry: JournalEntry?) {
        _currentEntry.value = entry
        _draftTitle.value = entry?.title ?: ""
        _draftContent.value = entry?.content ?: ""
        _draftImages.value = entry?.images ?: emptyList()
    }

    fun updateTitle(title: String) {
        _draftTitle.value = title
    }

    fun updateContent(content: String) {
        _draftContent.value = content
    }

    fun addImages(uris: List<Uri>) {
        _draftImages.update { it + uris }
    }

    fun removeImage(uri: Uri) {
        _draftImages.update { it - uri }
    }

    fun saveEntry() {
        viewModelScope.launch {
            val title = _draftTitle.value
            val content = _draftContent.value
            val images = _draftImages.value

            val entry = _currentEntry.value?.copy(
                title = title,
                content = content,
                images = images,
                timestamp = System.currentTimeMillis()
            ) ?: JournalEntry(
                title = title,
                content = content,
                images = images,
                timestamp = System.currentTimeMillis()
            )

            if (entry.id == null) {
                repository.insertJournalEntry(entry)
            } else {
                repository.updateJournalEntry(entry)
            }
            clearDraft()
        }
    }

    private fun clearDraft() {
        _currentEntry.value = null
        _draftTitle.value = ""
        _draftContent.value = ""
        _draftImages.value = emptyList()
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
        }
    }
}
