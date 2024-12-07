package com.ar.webwiz.utils.function

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri

fun getDefaultBrowser(context: Context): String {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
    val resolveInfo: ResolveInfo? = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

    return resolveInfo?.let {
        try {
            val appLabel = pm.getApplicationLabel(resolveInfo.activityInfo.applicationInfo).toString()
            appLabel
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    } ?: "Chrome"
}