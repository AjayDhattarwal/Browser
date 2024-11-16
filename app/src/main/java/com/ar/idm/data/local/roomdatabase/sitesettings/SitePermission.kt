package com.ar.idm.data.local.roomdatabase.sitesettings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "site_permissions")
data class SitePermission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val siteUrl: String,

    val notificationAllowed: Boolean = false,
    val cameraAllowed: Boolean = false,
    val microphoneAllowed: Boolean = false,
    val locationAllowed: Boolean = false,
    val nfcAllowed: Boolean = false,
    val usbAllowed: Boolean = false,
    val motionSensorAllowed: Boolean = false,
    val popupsAllowed: Boolean = false,
    val fullscreenAllowed: Boolean = false,
    val autoplayMediaAllowed: Boolean = false,
    val clipboardAccessAllowed: Boolean = false,
    val cookiesAllowed: Boolean = false,
    val storageAccessAllowed: Boolean = false
)
