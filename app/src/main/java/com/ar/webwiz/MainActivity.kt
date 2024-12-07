package com.ar.webwiz

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
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.documentfile.provider.DocumentFile
import com.ar.webwiz.utils.download.DownloadBroadcastReceiver
import com.ar.webwiz.ui.components.PermissionHandler
import com.ar.webwiz.ui.navigation.NavigationGraph
import com.ar.webwiz.ui.theme.IDMTheme
import com.ar.webwiz.viewmodel.BrowserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val downloadBroadcastReceiver = DownloadBroadcastReceiver()
    private val webViewViewModel: BrowserViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)


        intent?.data?.let { uri ->
            val key = "browser://browser/"
            val url = uri.toString()
            if(!url.startsWith(key)){
                webViewViewModel.addTab()
            }
        }


        enableEdgeToEdge()
        setContent {
            val state by webViewViewModel.state.collectAsState()
            splashScreen.setKeepOnScreenCondition {
               state.regularTabs.isEmpty()
            }
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
