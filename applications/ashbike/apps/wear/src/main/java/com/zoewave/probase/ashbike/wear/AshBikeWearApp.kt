package com.zoewave.probase.ashbike.wear

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AshBikeWearApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // Wear OS devices are all API 26+, but the check is good practice
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "bike_ride_channel" // Ensure this matches your Service constant
            val name = "Bike Ride Updates"
            val descriptionText = "Notifications for active bike rides"
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}