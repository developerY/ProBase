package com.zoewave.probase.ashbike.mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AshBikeApp : Application() {

    // TODO: REMOVE DEAD CODE
    /* override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // Channel for Bike Ride Service
        val bikeServiceChannelName = "Bike Ride Updates"
        val bikeServiceChannelDescription = "Notifications for active bike rides"
        val bikeServiceChannelImportance = NotificationManager.IMPORTANCE_LOW
        val bikeServiceChannel = NotificationChannel(
            BikeForegroundService.NOTIFICATION_CHANNEL_ID, // Using the constant from your service
            bikeServiceChannelName,
            bikeServiceChannelImportance
        ).apply {
            description = bikeServiceChannelDescription
        }

        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(bikeServiceChannel)
    }*/
}