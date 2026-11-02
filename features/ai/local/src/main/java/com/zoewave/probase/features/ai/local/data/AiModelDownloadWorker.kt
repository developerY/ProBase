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
        // Using ungated Qwen2.5 1.5B Instruct model from litert-community
        const val MODEL_URL = "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm"
        const val EXPECTED_CHECKSUM = "2ac49902133f729199bbd23ad6714df32b20585c786f2546d2293cc371f35278" // SHA-256 for Qwen2.5-1.5B
        const val EXPECTED_SIZE = 1_600_000_000L // ~1.6GB

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
            try {
                val uuid = storageManager.getUuidForPath(context.filesDir)
                val allocatable = storageManager.getAllocatableBytes(uuid)
                
                if (allocatable < (EXPECTED_SIZE * 1.2).toLong()) {
                    setProgress(workDataOf(STATE_KEY to "ERROR_STORAGE"))
                    return@withContext Result.failure(workDataOf("reason" to "Insufficient storage space."))
                }
            } catch (e: Exception) {
                // Ignore test environments where getUuidForPath might fail (e.g. Robolectric)
            }
        }

        val targetFile = File(context.filesDir, "model.litertlm")
        val partFile = File(context.filesDir, "model.litertlm.part")
        
        setProgress(workDataOf(STATE_KEY to "DOWNLOADING", PROGRESS_KEY to 0, BYTES_DOWNLOADED_KEY to 0L, TOTAL_BYTES_KEY to EXPECTED_SIZE))
        setForeground(createForegroundInfo(0, "Starting download..."))

        if (targetFile.exists()) {
            if (verifyChecksum(targetFile, EXPECTED_CHECKSUM)) {
                setProgress(workDataOf(STATE_KEY to "READY", "path" to targetFile.absolutePath))
                return@withContext Result.success(workDataOf("path" to targetFile.absolutePath))
            } else {
                targetFile.delete()
            }
        }

        var downloaded = if (partFile.exists()) partFile.length() else 0L

        try {
            // Using litert-community's official ungated weights for Qwen 1.5B INT8
            val urlString = inputData.getString("MODEL_URL") ?: MODEL_URL
            val url = URL(urlString)
            var connection = url.openConnection() as HttpURLConnection
            
            // Modern CDNs often require User-Agent to prevent 403 Forbidden / 404 Not Found blocks
            connection.setRequestProperty("User-Agent", "ProbaseAiModelDownloader/1.0")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.instanceFollowRedirects = true
            
            if (downloaded > 0) {
                connection.setRequestProperty("Range", "bytes=$downloaded-")
            }

            connection.connect()

            var responseCode = connection.responseCode
            
            // Capture ETag and Exact Size from the Hugging Face redirect response before following to S3
            var expectedChecksum = EXPECTED_CHECKSUM
            val xLinkedEtag = connection.getHeaderField("X-Linked-ETag")
            if (!xLinkedEtag.isNullOrBlank()) {
                expectedChecksum = xLinkedEtag.removePrefix("W/").removePrefix("\"").removeSuffix("\"")
            }
            val exactTotalBytes = connection.getHeaderField("X-Linked-Size")?.toLongOrNull()
            
            // Handle redirects manually if cross-origin (e.g. Hugging Face CDN resolving to S3)
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_SEE_OTHER || responseCode == 307 || responseCode == 308) {
                val redirectUrl = connection.getHeaderField("Location")
                if (redirectUrl != null) {
                    connection.disconnect()
                    connection = URL(redirectUrl).openConnection() as HttpURLConnection
                    connection.setRequestProperty("User-Agent", "ProbaseAiModelDownloader/1.0")
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    if (downloaded > 0) {
                        connection.setRequestProperty("Range", "bytes=$downloaded-")
                    }
                    connection.connect()
                    responseCode = connection.responseCode
                }
            }
            val contentLength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connection.contentLengthLong
            } else {
                connection.contentLength.toLong()
            }
            
            // Use exact server-reported byte length if available; otherwise calculate from content-length
            val totalBytes = exactTotalBytes ?: if (responseCode == HttpURLConnection.HTTP_PARTIAL) downloaded + contentLength else contentLength
            
            android.util.Log.d("AiModelDownloadWorker", "Starting download: URL=$url, responseCode=$responseCode, contentLength=$contentLength, downloaded=$downloaded, expectedChecksum=$expectedChecksum")

            if (responseCode == 401 || responseCode == 403 || responseCode == 404) {
                // Unrecoverable HTTP errors
                setProgress(workDataOf(STATE_KEY to "ERROR", "reason" to "HTTP Error: $responseCode"))
                return@withContext Result.failure(workDataOf("reason" to "HTTP Error: $responseCode"))
            }

            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                // To help debug the retry state, emit the error state before returning Retry
                setProgress(workDataOf(STATE_KEY to "ERROR", "reason" to "HTTP Response Code: $responseCode"))
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

            // Note: Verify exact totalBytes here, not the rough EXPECTED_SIZE constant
            if (partFile.length() == totalBytes && verifyChecksum(partFile, expectedChecksum)) { 
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
            // Return failure instead of retry for immediate UI feedback. The user can manually click 'Retry'.
            val errorMsg = e.message ?: "Unknown Exception"
            setProgress(workDataOf(STATE_KEY to "ERROR", "reason" to errorMsg))
            Result.failure(workDataOf("reason" to errorMsg))
        }
    }

    private fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
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
            hash == expectedChecksum
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
