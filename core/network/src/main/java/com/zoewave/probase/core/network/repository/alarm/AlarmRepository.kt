package com.zoewave.probase.core.network.repository.alarm

import com.zoewave.probase.core.model.alarm.ProAlarm
import kotlinx.coroutines.flow.Flow


interface AlarmRepository {
    fun setupNotificationChannel()
    suspend fun addAlarm(proAlarm: ProAlarm)
    suspend fun deleteAlarm(alarmId: Int)
    suspend fun clearAllAlarms()
    suspend fun saveAlarms(alarms: List<ProAlarm>)
    suspend fun toggleAlarm(alarmId: Int, isEnabled: Boolean)
    fun getAlarms(): Flow<List<ProAlarm>>
    suspend fun removeAlarm(alarmId: Int)

    // debug
    fun logAlarms()
}

