package com.ar.webwiz.utils.internalFunction

import android.app.PendingIntent
import android.app.RemoteAction
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import com.ar.webwiz.MyApp.Companion.PLAY_PAUSE_ACTION


fun actionsPip(context: Context, isPlaying: Boolean): List<RemoteAction> {
    val playIntent = PendingIntent.getBroadcast(
        context, 2001, Intent(PLAY_PAUSE_ACTION), PendingIntent.FLAG_IMMUTABLE
    )

    val pauseIcon = Icon.createWithResource(context, android.R.drawable.ic_media_pause )
    val playIcon = Icon.createWithResource(context, android.R.drawable.ic_media_play )

    val playAction = RemoteAction(playIcon, "Play", "Play", playIntent)
    val pauseAction = RemoteAction(pauseIcon, "Pause", "Pause", playIntent)

    return listOf( if(isPlaying) pauseAction else playAction  )


}

