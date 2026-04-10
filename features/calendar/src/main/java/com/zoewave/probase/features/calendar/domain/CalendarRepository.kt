package com.zoewave.probase.features.calendar.domain

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for interacting with the system calendar.
 */
internal interface CalendarRepository {
    fun queryEvents(startTime: Long, endTime: Long): Flow<List<CalendarEventModel>>
    suspend fun insertEvent(event: CalendarEventModel): Long?
    suspend fun updateEvent(event: CalendarEventModel): Boolean
    suspend fun deleteEvent(eventId: Long): Boolean
}
