package com.ar.webwiz.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ar.webwiz.MyApp.Companion.PLAY_PAUSE_ACTION

class PipActionReceiver(
    private val onPlayPause: () -> Unit,
) : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            PLAY_PAUSE_ACTION -> {
                onPlayPause()
            }

        }
    }
}