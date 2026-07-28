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

    fun onEntrySelected(entry: JournalEntry?) {
        _currentEntry.value = entry
    }

    fun saveEntry(title: String, content: String, images: List<Uri>) {
        viewModelScope.launch {
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
            _currentEntry.value = null
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
        }
    }
}
