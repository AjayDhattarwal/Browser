package com.ar.idm.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat

@Composable
fun PermissionHandler(
    permission: String,
    minSdkVersion: Int = Build.VERSION_CODES.P,
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {},
) {
    val context = LocalContext.current
    val currentSdkVersion = Build.VERSION.SDK_INT


    val isPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        permission
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    LaunchedEffect(key1 = currentSdkVersion) {
        if (currentSdkVersion >= minSdkVersion) {
            if (!isPermissionGranted) {
                permissionLauncher.launch(permission)
            } else {
                onPermissionGranted()
            }
        }
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

