package com.ar.idm.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.ar.idm.utils.function.bundleToString
import com.google.gson.Gson

class BundleStatePreference(context: Context){

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("webview_state_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    suspend fun updateWebViewState(bundle: Bundle, id: String) {
        try {
            val string = bundleToString(bundle)
            sharedPreferences.edit().putString("webview_$id", string).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getBundleStateString(id: String): String? {
        return sharedPreferences.getString("webview_$id", null)
    }

    fun deleteWebViewState(id: String) {
        sharedPreferences.edit().remove("webview_$id").apply()

    }

    fun getCurrentTabTag(): String?{
        return sharedPreferences.getString("current_tab_index", null)
    }
    fun setCurrentTabTag(tag: String){
        sharedPreferences.edit().putString("current_tab_index", tag).apply()

    }
}