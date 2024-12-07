package com.ar.webwiz.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.ar.webwiz.domain.repository.TabKey
import com.ar.webwiz.utils.function.bundleToString
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BundleStatePreference(context: Context){

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("webview_state_prefs", Context.MODE_PRIVATE)

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

    fun addTabToPreferences(newTabKey: TabKey) {
        val gson = Gson()
        val jsonString = sharedPreferences.getString("TabKey", null)

        // Retrieve the existing list
        val type = object : TypeToken<List<TabKey>>() {}.type
        val existingList: MutableList<TabKey> = if (jsonString != null) {
            gson.fromJson(jsonString, type)
        } else {
            mutableListOf()
        }

        // Ensure the tab is unique by its tag
        if (existingList.none { it.tag == newTabKey.tag }) {
            existingList.add(newTabKey)
        }

        val updatedJsonString = gson.toJson(existingList)
        sharedPreferences.edit().putString("TabKey", updatedJsonString).apply()
    }


    fun getTabsFromPreferences(): List<TabKey> {
        val gson = Gson()
        val jsonString = sharedPreferences.getString("TabKey", null) ?: return emptyList()
        val type = object : TypeToken<List<TabKey>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun removeTabByTag(tag: String) {
        val gson = Gson()
        val jsonString = sharedPreferences.getString("TabKey", null)

        val type = object : TypeToken<List<TabKey>>() {}.type
        val existingList: MutableList<TabKey> = if (jsonString != null) {
            gson.fromJson(jsonString, type)
        } else {
            mutableListOf()
        }

        val updatedList = existingList.filter { it.tag != tag }

        val updatedJsonString = gson.toJson(updatedList)
        sharedPreferences.edit().putString("TabKey", updatedJsonString).apply()
    }



}