package com.zoewave.probase.applications.journal.data

import android.net.Uri
import com.zoewave.probase.applications.journal.database.dao.JournalDao
import com.zoewave.probase.applications.journal.database.entity.JournalEntryEntity
import com.zoewave.probase.applications.journal.model.JournalEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val dao: JournalDao
) : JournalRepository {

    override fun getJournalEntries(): Flow<List<JournalEntry>> {
        return dao.getJournalEntries().map { entities ->
            entities.map { it.toJournalEntry() }
        }
    }

    override suspend fun getJournalEntryById(id: Long): JournalEntry? {
        return dao.getJournalEntryById(id)?.toJournalEntry()
    }

    override suspend fun insertJournalEntry(entry: JournalEntry) {
        dao.insertJournalEntry(entry.toJournalEntryEntity())
    }

    override suspend fun updateJournalEntry(entry: JournalEntry) {
        dao.updateJournalEntry(entry.toJournalEntryEntity())
    }

    override suspend fun deleteJournalEntry(entry: JournalEntry) {
        dao.deleteJournalEntry(entry.toJournalEntryEntity())
    }

    private fun JournalEntryEntity.toJournalEntry(): JournalEntry {
        return JournalEntry(
            id = id,
            title = title,
            content = content,
            timestamp = timestamp,
            images = if (images.isBlank()) emptyList() else images.split(",").map { Uri.parse(it) }
        )
    }

    private fun JournalEntry.toJournalEntryEntity(): JournalEntryEntity {
        return JournalEntryEntity(
            id = id,
            title = title,
            content = content,
            timestamp = timestamp,
            images = images.joinToString(",") { it.toString() }
        )
    }
}
