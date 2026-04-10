package com.zoewave.probase.features.calendar.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.zoewave.probase.features.calendar.data.db.dao.CalendarSyncDao
import com.zoewave.probase.features.calendar.domain.CalendarEventModel
import com.zoewave.probase.features.calendar.domain.CalendarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.TimeZone
import javax.inject.Inject

/**
 * Implementation of [CalendarRepository] using the Android Calendar Provider API.
 */
class AndroidCalendarProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calendarSyncDao: CalendarSyncDao // Isolated DAO injected here
) : CalendarRepository {

    private val contentResolver: ContentResolver get() = context.contentResolver

    override fun queryEvents(startTime: Long, endTime: Long): Flow<List<CalendarEventModel>> = flow {
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.ALL_DAY
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(startTime.toString(), endTime.toString())

        val events = mutableListOf<CalendarEventModel>()
        
        contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                events.add(
                    CalendarEventModel(
                        id = cursor.getLong(0),
                        calendarId = cursor.getLong(1),
                        title = cursor.getString(2),
                        description = cursor.getString(3),
                        startTimeMillis = cursor.getLong(4),
                        endTimeMillis = cursor.getLong(5),
                        location = cursor.getString(6),
                        isAllDay = cursor.getInt(7) != 0
                    )
                )
            }
        }
        emit(events)
    }.flowOn(Dispatchers.IO)

    override suspend fun insertEvent(event: CalendarEventModel): Long? = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, event.startTimeMillis)
            put(CalendarContract.Events.DTEND, event.endTimeMillis)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.CALENDAR_ID, event.calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
        }

        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        uri?.lastPathSegment?.toLongOrNull()
    }

    override suspend fun updateEvent(event: CalendarEventModel): Boolean = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, event.startTimeMillis)
            put(CalendarContract.Events.DTEND, event.endTimeMillis)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
        }

        val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
        val rows = contentResolver.update(updateUri, values, null, null)
        rows > 0
    }

    override suspend fun deleteEvent(eventId: Long): Boolean = withContext(Dispatchers.IO) {
        val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        val rows = contentResolver.delete(deleteUri, null, null)
        rows > 0
    }
}
