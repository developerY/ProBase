package com.zoewave.probase.ashbike.features.main.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.zoewave.ashbike.model.bike.BikeRideInfo
import com.zoewave.probase.ashbike.features.main.R

/**
 * Handles the creation and updates of the Foreground Service Notification.
 */
class BikeNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    fun buildNotification(rideState: BikeRideInfo): Notification {
        val pendingIntent = getContentIntent()

        val currentSpeedFormatted = String.format("%.1f", rideState.currentSpeed)
        val currentDistanceFormatted = String.format("%.1f", rideState.currentTripDistance)
        // val currentElevationGainFormatted = String.format("%.0f", rideState.elevationGain)

        val notificationText = "Speed: $currentSpeedFormatted km/h  •  Dist: $currentDistanceFormatted km"

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("AshBike Active")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_bike) // Ensure this icon exists
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun updateNotification(notificationId: Int, rideState: BikeRideInfo) {
        notificationManager.notify(notificationId, buildNotification(rideState))
    }

    fun cancel(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    private fun getContentIntent(): PendingIntent {
        val packageName = context.packageName
        val activityIntent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getActivity(context, 0, activityIntent, flags)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Bike Ride Tracking",
                NotificationManager.IMPORTANCE_LOW // Low importance prevents "peeking" sounds
            ).apply {
                description = "Shows active bike ride statistics"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "ashbike_ride_channel_v1"
        const val NOTIFICATION_ID = 101
    }
}