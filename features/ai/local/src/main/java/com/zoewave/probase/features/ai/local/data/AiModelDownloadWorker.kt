package com.zoewave.probase.features.ai.local.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.storage.StorageManager
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class AiModelDownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val MODEL_URL = "https://storage.googleapis.com/ko_color_models/gemma_1b_int8.litertlm" // Example URL
        const val EXPECTED_CHECKSUM = "YOUR_EXPECTED_SHA256_CHECKSUM_HERE" // Replace with real checksum
        const val EXPECTED_SIZE = 1_500_000_000L // Example size ~ 1.5GB

        const val PROGRESS_KEY = "PROGRESS"
        const val BYTES_DOWNLOADED_KEY = "BYTES_DOWNLOADED"
        const val TOTAL_BYTES_KEY = "TOTAL_BYTES"
        const val STATE_KEY = "STATE"

        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "ai_model_download_channel"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val uuid = storageManager.getUuidForPath(context.filesDir)
            val allocatable = storageManager.getAllocatableBytes(uuid)
            
            if (allocatable < (EXPECTED_SIZE * 1.2).toLong()) {
                setProgress(workDataOf(STATE_KEY to "ERROR_STORAGE"))
                return@withContext Result.failure(workDataOf("reason" to "Insufficient storage space."))
            }
        }

        setForeground(createForegroundInfo(0, "Starting download..."))

        val targetFile = File(context.filesDir, "model.litertlm")
        val partFile = File(context.filesDir, "model.litertlm.part")

        if (targetFile.exists()) {
            if (verifyChecksum(targetFile)) {
                setProgress(workDataOf(STATE_KEY to "READY", "path" to targetFile.absolutePath))
                return@withContext Result.success(workDataOf("path" to targetFile.absolutePath))
            } else {
                targetFile.delete()
            }
        }

        var downloaded = if (partFile.exists()) partFile.length() else 0L

        try {
            val url = URL(MODEL_URL)
            val connection = url.openConnection() as HttpURLConnection
            if (downloaded > 0) {
                connection.setRequestProperty("Range", "bytes=$downloaded-")
            }

            connection.connect()

            val responseCode = connection.responseCode
            val contentLength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connection.contentLengthLong
            } else {
                connection.contentLength.toLong()
            }

            val totalBytes = if (responseCode == HttpURLConnection.HTTP_PARTIAL) downloaded + contentLength else contentLength

            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                return@withContext Result.retry()
            }

            connection.inputStream.use { input ->
                partFile.outputStream().apply {
                    if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                        // Append mode not strictly needed if we just open stream to append but typical outputStream(append=true) or RandomAccessFile
                        // For simplicity let's use FileOutputStream with append
                    }
                }.close() // Just to clear

                // Real append:
                val output = java.io.FileOutputStream(partFile, responseCode == HttpURLConnection.HTTP_PARTIAL)
                output.use { out ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var lastUpdate = System.currentTimeMillis()

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                        downloaded += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 500) {
                            lastUpdate = now
                            val progress = ((downloaded.toFloat() / totalBytes) * 100).toInt()
                            setProgress(
                                workDataOf(
                                    STATE_KEY to "DOWNLOADING",
                                    PROGRESS_KEY to progress,
                                    BYTES_DOWNLOADED_KEY to downloaded,
                                    TOTAL_BYTES_KEY to totalBytes
                                )
                            )
                            setForeground(createForegroundInfo(progress, "Downloading... $progress%"))
                        }
                    }
                }
            }

            setProgress(workDataOf(STATE_KEY to "VERIFYING"))
            setForeground(createForegroundInfo(100, "Verifying checksum..."))

            if (partFile.length() == totalBytes && verifyChecksum(partFile)) { // Replace with real verify
                partFile.renameTo(targetFile)
                setProgress(workDataOf(STATE_KEY to "READY", "path" to targetFile.absolutePath))
                Result.success(workDataOf("path" to targetFile.absolutePath))
            } else {
                partFile.delete()
                setProgress(workDataOf(STATE_KEY to "ERROR_CHECKSUM"))
                Result.failure(workDataOf("reason" to "Checksum verification failed."))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun verifyChecksum(file: File): Boolean {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            val hash = md.digest().joinToString("") { "%02x".format(it) }
            hash == EXPECTED_CHECKSUM
        } catch (e: Exception) {
            false
        }
    }

    private fun createForegroundInfo(progress: Int, message: String): ForegroundInfo {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI Model Download",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Downloading AI Model")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
}
