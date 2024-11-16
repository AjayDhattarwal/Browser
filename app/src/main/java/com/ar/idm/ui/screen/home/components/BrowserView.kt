@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ar.idm.ui.screen.home.components

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Context
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.trace
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.ar.idm.MyApp.Companion.PLAY_PAUSE_ACTION
import com.ar.idm.R
import com.ar.idm.domain.model.BrowserState
import com.ar.idm.utils.PipActionReceiver
import com.ar.idm.utils.internalFunction.actionsPip
import com.ar.idm.utils.internalFunction.findActivity
import com.ar.idm.utils.youtube.toggleYTControls
import kotlinx.coroutines.flow.StateFlow


@Composable
fun BrowserView(
    browserState: StateFlow<BrowserState>,
    modifier: Modifier,
) = trace("BrowserView") {

    val state by browserState.collectAsState()
    val context = LocalContext.current

    val isFullscreen by remember { derivedStateOf { state.currentTab?.isFullScreen ?: false } }

    var isPlaying by remember { mutableStateOf(true) }


    val pipReceiver = remember {
        PipActionReceiver(
            onPlayPause = {
                val script = if(isPlaying)
                    "document.querySelector('video').pause();"
                else
                    "document.querySelector('video').play();"

                state.currentTab?.webView?.evaluateJavascript(script, null)
                isPlaying = !isPlaying

            }
        )
    }
    val filter = IntentFilter().apply {
        addAction(PLAY_PAUSE_ACTION)
    }

    val actions = actionsPip(context, isPlaying = isPlaying)


    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && isFullscreen) {
        DisposableEffect(context) {
            val onUserLeaveBehavior: () -> Unit = {
                context.findActivity()
                    .enterPictureInPictureMode(
                        PictureInPictureParams.Builder()
                            .setActions(actions)
                            .build()
                    )
            }
            context.findActivity().addOnUserLeaveHintListener(
                onUserLeaveBehavior
            )
            onDispose {
                context.findActivity().removeOnUserLeaveHintListener(
                    onUserLeaveBehavior
                )
            }
        }
    } else {
        Log.i("PiP info", "API does not support PiP")
    }



    val pipModifier = Modifier
        .onGloballyPositioned { layoutCoordinates ->
            if (isFullscreen){
                val builder = PictureInPictureParams.Builder().setActions(actions)
                val componentActivity = context.findActivity()

                componentActivity.registerReceiver(pipReceiver, filter, Context.RECEIVER_EXPORTED)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    builder.setAutoEnterEnabled(true)
                }
                componentActivity.setPictureInPictureParams(builder.build())
            }
        }


    Box(
        modifier = modifier.fillMaxSize()
    ) {
        key(state.currentTab?.webView?.tag) {
            AndroidView(
                factory = {
                    state.currentTab?.webView?.apply {
                        setupContentTheming()
                        this.onResume()
                        toggleYTControls()
                    } ?: WebView(it)
                },
                update = { webView ->
                    webView.setupContentTheming()
                    webView.evaluateJavascript(
                        "document.querySelector('video').paused;",
                        { value ->
                            isPlaying = (value == "false")
                        }
                    )

                },
                modifier = pipModifier.fillMaxSize()

            )
            if(!isFullscreen){
                LoadingBar(browserState = browserState)
            }
        }

    }
}

@SuppressLint("RequiresFeature")
fun WebView.setupContentTheming() {
    val isDarkTheme = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    if (isDarkTheme) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            Log.d("TAG", "setupContentTheming for true: $isDarkTheme")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                settings.isAlgorithmicDarkeningAllowed = true
            }
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)

        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            @Suppress("DEPRECATION")
            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
        }
    } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            Log.d("TAG", "setupContentTheming for false: $isDarkTheme")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                settings.isAlgorithmicDarkeningAllowed = false
            }
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, false)
        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            @Suppress("DEPRECATION")
            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
        }
    }
}





