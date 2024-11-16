package com.ar.idm.utils.webview

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.ar.idm.data.local.preferences.PreferencesManager

class WebAppInterface(private val context: Context) {
    val preferencesManager = PreferencesManager(context)

    @JavascriptInterface
    fun saveUserData(userName: String?, userEmail: String?, userImage: String?) {
        Log.d("WebAppInterface", "Saving user data: $userName, $userEmail, $userImage")
        preferencesManager.saveUserProfile(userName, userEmail, userImage)
    }

    @JavascriptInterface
    fun showToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
