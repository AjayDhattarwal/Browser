package com.ar.idm.domain.model

import android.view.View
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebView
import androidx.compose.ui.graphics.ImageBitmap
import com.ar.idm.utils.function.generateFaviconUrl



data class TabState(
    val webView: WebView,
    val view: View? = null,
    val callback: CustomViewCallback? = null,
    val preview: ImageBitmap? = null,
    val showOverlay: Boolean? = true,
    val url : String? = null,
    val title: String? = null,
    val parentTag : String? = null,
    val totalSearchMatches: Int? = null,
    val currentSearchMatchIndex: Int? = null,
    val searchQuery: String? = null,
    val isIncognito: Boolean = false
){
    val favIconUrl: String
        get() = generateFaviconUrl(url)

    val isFullScreen: Boolean
        get() = view != null

    val hasParent : Boolean
        get() = parentTag != null
}
