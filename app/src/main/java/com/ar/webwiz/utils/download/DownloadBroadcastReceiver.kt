package com.ar.webwiz.utils.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

class DownloadBroadcastReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {

        val downloadRepository: DownloadRepository = getKoin().get()

        when (intent?.action) {
            "ACTION_PAUSE_DOWNLOAD" -> {
                val workerId = intent.getStringExtra("worker_uuid_key")
                if (workerId != null) {
                    Log.d("DownloadBroadcastReceiver","Pause action received Worker id is : $workerId")
                    GlobalScope.launch(Dispatchers.IO) {
                        downloadRepository.pauseOrResumeDownload(workerId)
                    }

                }

            }
            "ACTION_CANCEL" -> {
                val workerId = intent.getStringExtra("worker_uuid_key")
                if (workerId != null) {
                    Log.d("DownloadBroadcastReceiver","Cancel action received Worker id is : $workerId")
                    downloadRepository.cancelDownload(workerId)
                }
            }
        }
    }
}