package com.zoewave.probase.rxlogic.features.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val medicationName = inputData.getString("medication_name") ?: "Medication"
        
        // In a real app, we would show a notification here.
        // For this task, we'll just log it.
        println("Reminder for $medicationName")
        
        return Result.success()
    }
}
