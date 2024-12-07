package com.ar.webwiz.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ar.webwiz.domain.model.UserProfile

class PreferencesManager(context: Context) {

    companion object {
        private const val PREFERENCES_NAME = "browser_settings"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PROFILE_IMAGE = "user_profile_image"
        private const val KEY_THEME = "theme"
        private const val KEY_SEARCH_ENGINE = "search_engine"
        private const val DEFAULT_THEME = "light"
        private const val DEFAULT_SEARCH_ENGINE = "Google"
        private const val KEY_COOKIE = "session_cookie"
        private const val KEY_DATA_SYNC_STATUS = "data_sync_status"
        private const val KEY_ADS_BLOCKED = "ads_blocked"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
    }



    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)


    fun saveUserProfile(userName: String?, userEmail: String?, userProfileImage: String?){
        preferences.edit().putString(KEY_USER_NAME, userName).apply()
        preferences.edit().putString(KEY_USER_EMAIL, userEmail).apply()
        preferences.edit().putString(KEY_USER_PROFILE_IMAGE, userProfileImage
            ?.replace("s32", "s300")).apply()
    }

    fun getUserProfile(): UserProfile{
        return UserProfile(
            preferences.getString(KEY_USER_NAME, "") ?: "",
            preferences.getString(KEY_USER_EMAIL, null),
            preferences.getString(KEY_USER_PROFILE_IMAGE, "") ?: ""
        )
    }
    fun toggleDataSyncStatus() {
        val status = getDataSyncStatus() == "OFF"
        preferences.edit().putString(KEY_DATA_SYNC_STATUS, if(status) "ON" else "OFF").apply()
    }

    fun getDataSyncStatus(): String {
        return if(preferences.getString(KEY_USER_EMAIL, null) == null){
            "OFF"
        } else{
            preferences.getString(KEY_DATA_SYNC_STATUS, "OFF") ?: "OFF"
        }
    }

    fun setDesktopMode(boolean: Boolean) {
        preferences.edit().putBoolean(KEY_DESKTOP_MODE, boolean).apply()
    }

    fun getDesktopModeStatus(): Boolean {
        return preferences.getBoolean(KEY_DESKTOP_MODE, false)
    }

    fun setTheme(isDarkMode: Boolean) {
        preferences.edit().putString(KEY_THEME, if (isDarkMode) "dark" else "light").apply()
    }

    fun getTheme(): String {
        return preferences.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
    }

    fun setSearchEngine(searchEngine: String) {
        preferences.edit().putString(KEY_SEARCH_ENGINE, searchEngine).apply()
    }

    fun getSearchEngine(): String {
        return preferences.getString(KEY_SEARCH_ENGINE, DEFAULT_SEARCH_ENGINE) ?: DEFAULT_SEARCH_ENGINE
    }

    fun setAdsBlocked(adsBlocked: Boolean) {
        preferences.edit().putString(KEY_ADS_BLOCKED, if (adsBlocked) "enable" else "disable").apply()
    }

    fun getAdsBlocked(): String {
        return preferences.getString(KEY_ADS_BLOCKED, "enable") ?: "enable"
    }


    fun clearAllSettings() {
        preferences.edit().clear().apply()
    }

    fun setCookie(cookie: String) {
        preferences.edit().putString(KEY_COOKIE, cookie).apply()
    }

    fun getCookie(): String? {
        return preferences.getString(KEY_COOKIE, null)
    }
}
