package com.ar.idm

import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.documentfile.provider.DocumentFile
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.ar.idm.data.SaveDataWorker
import com.ar.idm.utils.download.DownloadBroadcastReceiver
import com.ar.idm.ui.components.PermissionHandler
import com.ar.idm.ui.navigation.NavigationGraph
import com.ar.idm.ui.theme.IDMTheme
import com.ar.idm.viewmodel.BrowserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.androidx.viewmodel.factory.KoinViewModelFactory

class MainActivity : ComponentActivity() {
    private val downloadBroadcastReceiver = DownloadBroadcastReceiver()
    private val webViewViewModel: BrowserViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        intent?.data?.let { uri ->
            val url = uri.toString()
            webViewViewModel.addTab(url)
        }
        enableEdgeToEdge()
        setContent {
            IDMTheme {
                Surface{
                    PermissionHandler(
                        permission = "android.permission.POST_NOTIFICATIONS",
                        minSdkVersion = Build.VERSION_CODES.TIRAMISU
                    )
                    NavigationGraph(webViewViewModel)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter().apply {
            addAction("ACTION_PAUSE")
            addAction("ACTION_CANCEL")
        }
        registerReceiver(downloadBroadcastReceiver, intentFilter, RECEIVER_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(downloadBroadcastReceiver)
    }




    override fun onPause() {
        super.onPause()
        webViewViewModel.onPaused()
    }

    override fun onDestroy() {
        super.onDestroy()
        webViewViewModel.onPaused()
        Log.d("MainActivity", "onDestroy: ")
    }


}




fun getRealPathFromURI(context: Context, uri: Uri): String? {
    val documentFile = DocumentFile.fromTreeUri(context, uri)
    return documentFile?.uri?.path?.replace("/tree/primary:", "/storage/emulated/0/")
}
