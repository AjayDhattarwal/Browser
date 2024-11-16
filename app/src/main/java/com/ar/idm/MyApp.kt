package com.ar.idm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.ar.idm.di.appModule
import com.ar.idm.di.databaseModule
import com.ar.idm.di.networkModule
import com.ar.idm.di.repositoryModule
import com.ar.idm.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {

    companion object {
        const val PLAY_PAUSE_ACTION = "ACTION_PLAY_PAUSE"
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(appModule,networkModule, viewModelModule, databaseModule, repositoryModule)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "download_channel"
            val channelName = "Download Notifications"
            val channelDescription = "Notifications for download progress"

            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

        }
    }

}