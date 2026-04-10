package com.zoewave.probase.features.calendar.domain

/**
 * Domain model representing a system calendar event.
 */
data class CalendarEventModel(
    val id: Long,
    val calendarId: Long,
    val title: String,
    val description: String?,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val location: String?,
    val isAllDay: Boolean
)
