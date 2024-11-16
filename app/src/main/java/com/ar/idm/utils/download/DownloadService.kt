package com.ar.idm.utils.download

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

class DownloadService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationId = intent?.getIntExtra("notificationId", -1) ?: return START_NOT_STICKY
        val title = intent.getStringExtra("title") ?: "Downloading..."

        when (intent.action) {
            ACTION_START_DOWNLOAD -> {
                val content = intent.getStringExtra("content") ?: "Download in progress"
                val workerUuidKey = intent.getStringExtra("worker_uuid_key")?:""
                startForegroundService(
                    notificationId = notificationId,
                    title = title,
                    content = content,
                    progress = 0,
                    isPaused = false,
                    workerUuidKey =  workerUuidKey
                )
            }
            ACTION_UPDATE_PROGRESS -> {
                val progress = intent.getIntExtra("progress", 0)
                val content = intent.getStringExtra("content")?:"Download in progress"
                val isPaused = intent.getBooleanExtra("isPaused", false)
                val subText = intent.getStringExtra("subTitle")?:"calculating.."
                val workerUuidKey = intent.getStringExtra("worker_uuid_key")?:""
                updateNotification(
                    notificationId = notificationId,
                    title = title,
                    content = content,
                    progress = progress,
                    isPaused = isPaused,
                    subText = subText,
                    workerUuidKey = workerUuidKey
                )
            }
            ACTION_CANCEL_DOWNLOAD -> cancelDownload(notificationId)
            ACTION_DOWNLOAD_COMPLETE -> downloadComplete(notificationId,title)
        }
        return START_NOT_STICKY
    }



    private fun startForegroundService(notificationId: Int, title: String, content: String, progress: Int, isPaused: Boolean, workerUuidKey: String = "") {
        val notification = createNotification(
            notificationId,
            title,
            content,
            progress,
            isPaused,
            worker_uuid_key = workerUuidKey
        )
        startForeground(notificationId, notification)
    }

    private fun updateNotification(notificationId: Int,title: String, content: String, progress: Int, isPaused: Boolean, subText: String = "", workerUuidKey: String = "") {
        val notification = createNotification(notificationId, title, content, progress, isPaused, subText, workerUuidKey)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    private fun cancelDownload(notificationId: Int) {
        NotificationManagerCompat.from(this).cancel(notificationId)
        stopSelf()
    }

    private fun downloadComplete(notificationId: Int, title: String) {
        updateNotification(notificationId,title, "Download complete", 100, false,)
        stopSelf()
    }

    private fun createNotification(
        notificationId: Int,
        title: String,
        content: String,
        progress: Int,
        isPaused: Boolean,
        subtext: String = "",
        worker_uuid_key: String,
    ): Notification {

        val pauseToggleIntent = Intent(this, DownloadBroadcastReceiver::class.java).apply {
            action = "ACTION_PAUSE_DOWNLOAD"
            putExtra("notificationId", notificationId)
            putExtra("worker_uuid_key", worker_uuid_key)
        }
        val cancelIntent = Intent(this, DownloadBroadcastReceiver::class.java).apply {
            action = "ACTION_CANCEL"
            putExtra("notificationId", notificationId)
            putExtra("worker_uuid_key", worker_uuid_key)
        }

        val pauseTogglePendingIntent = PendingIntent.getBroadcast(this, notificationId + 1001, pauseToggleIntent, PendingIntent.FLAG_MUTABLE)
        val cancelPendingIntent = PendingIntent.getBroadcast(this, notificationId + 1002, cancelIntent, PendingIntent.FLAG_MUTABLE)


        val notificationBuilder = NotificationCompat.Builder(this, "download_channel")
            .setContentTitle(title)
            .setContentText(content)
            .setSubText(subtext)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
        if (progress < 100) {
            notificationBuilder.apply {
                if (isPaused) {
                    setSmallIcon(android.R.drawable.ic_media_pause)
                    addAction(android.R.drawable.ic_media_play, "Resume", pauseTogglePendingIntent)
                } else {
                    setProgress(100, progress, false)
                    setSmallIcon(android.R.drawable.stat_sys_download)
                    addAction(android.R.drawable.ic_media_pause, "Pause", pauseTogglePendingIntent)
                }
                addAction(android.R.drawable.ic_delete, "Cancel", cancelPendingIntent)
            }
        } else {
            notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setProgress(0, 0, false)
        }

        return notificationBuilder.build()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ACTION_START_DOWNLOAD = "com.example.ACTION_START_DOWNLOAD"
        const val ACTION_UPDATE_PROGRESS = "com.example.ACTION_UPDATE_PROGRESS"
        const val ACTION_PAUSE_DOWNLOAD = "com.example.ACTION_PAUSE_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.example.ACTION_CANCEL_DOWNLOAD"
        const val ACTION_DOWNLOAD_COMPLETE = "com.example.ACTION_DOWNLOAD_COMPLETE"
    }
}
