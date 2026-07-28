package com.zoewave.probase.applications.journal.data

import com.zoewave.probase.applications.journal.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun getJournalEntries(): Flow<List<JournalEntry>>
    suspend fun getJournalEntryById(id: Long): JournalEntry?
    suspend fun insertJournalEntry(entry: JournalEntry)
    suspend fun updateJournalEntry(entry: JournalEntry)
    suspend fun deleteJournalEntry(entry: JournalEntry)
}
