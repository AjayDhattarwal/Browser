package com.ar.idm.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.webkit.SafeBrowsingResponseCompat
import androidx.webkit.WebResourceRequestCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.internal.WebResourceRequestAdapter
import com.ar.idm.data.local.preferences.BundleStatePreference
import com.ar.idm.domain.model.BrowserState
import com.ar.idm.domain.model.TabState
import com.ar.idm.domain.repository.HistoryRepository
import com.ar.idm.domain.repository.ImageRepository
import com.ar.idm.domain.repository.TabStateRepository
import com.ar.idm.utils.download.DownloadRepository
import com.ar.idm.utils.webview.captureVisiblePartAndNavigate
import com.ar.idm.utils.webview.AdBlocker
import com.ar.idm.utils.function.readBundleFromFile
import com.ar.idm.utils.function.retrieveImageBitmap
import com.ar.idm.utils.webview.WebAppInterface
import com.ar.idm.utils.webview.configureWebView
import com.ar.idm.utils.webview.saveUserData
import com.ar.idm.utils.webview.youtubeAdBlockingScript
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.UUID
import kotlin.random.Random



class BrowserViewModel(
    private val context: Application,
    private val adBlocker: AdBlocker,
    private val downloadRepository: DownloadRepository,
    private val imageRepository: ImageRepository,
    private val tabStateRepository: TabStateRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {


    private val _state = MutableStateFlow(BrowserState())
    val state: StateFlow<BrowserState> get() = _state.asStateFlow()

    private val bundleStatePreference = BundleStatePreference(context)
    private val webAppInterface = WebAppInterface(context)

    private var mobileString = mutableStateOf("")

    private val desktopString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"

    init{
        viewModelScope.launch {
            mobileString.value = WebView(context).settings.userAgentString
            if (_state.value.regularTabs.isEmpty()) {

                adBlocker.initialize()

                val savedCurrentTabTag = bundleStatePreference.getCurrentTabTag()

                val deferredMutableTabs = tabStateRepository.getAllTab().sortedBy { it.index }.map {
                    async {
                        val webView = WebView(context).apply {
                            it.stateFilePath?.let { path ->
                                File(path).readBundleFromFile()?.let { bundle ->
                                    restoreState(bundle)

                                }
                            }
                            tag = it.webViewId
                        }
                        val fullWebView =
                            createWebView(
                                blank = true,
                                webView = webView,
                                isIncognito = false,
                                setTag = false
                            ).apply {
                                onPause()
                            }
                        val preview = it.thumbnail?.let { path ->
                            File(path).retrieveImageBitmap()
                        }
                        TabState(webView = fullWebView, preview = preview, showOverlay = it.overlay, url = webView.url, title = webView.title)
                    }
                }

                val mutableTabs = deferredMutableTabs.awaitAll().toMutableList() + _state.value.regularTabs
                val currentIndex = mutableTabs.indexOfFirst { it.webView.tag == savedCurrentTabTag }

                if (mutableTabs.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        regularTabs = mutableTabs,
                        regularTabIndex =  if(currentIndex == -1) mutableTabs.size - 1 else currentIndex,
                    )
                }else{
                    addTab()
                }

            }
        }
    }

    fun updateTabState(isIncognito: Boolean){
        _state.value = _state.value.copy(
            isIncognitoMode = isIncognito
        )
        CookieManager.getInstance() .apply {
            removeSessionCookies(null)
            flush()
            if (isIncognito) {
                setAcceptCookie(false)
            } else {
                setAcceptCookie(true)
            }
        }
    }



    fun addTab(url: String? = null, blank: Boolean = false, webView: WebView? = null, index: Int? = null, parentTag : String? =  null, isPrivate: Boolean? = null) {
        val isIncognito = isPrivate ?: _state.value.isIncognitoMode

        val webViewFullVersion = createWebView(url, blank, webView = webView ?: WebView(context), isIncognito = isIncognito)

        if(url != null){
            bundleStatePreference.setCurrentTabTag(webViewFullVersion.tag.toString())
            updatePreview(index = _state.value.currentMatchIndex, isPrivate = isIncognito)
        }

        val newTab = TabState(webView = webViewFullVersion, preview = null, showOverlay = url == null, parentTag = parentTag, isIncognito = isIncognito)

        val mutableTabs = (if(isIncognito) _state.value.incognitoTabs else _state.value.regularTabs).toMutableList()

        val insertionIndex = index ?: if (mutableTabs.isEmpty()) 0 else mutableTabs.size

        if (insertionIndex < 0 || insertionIndex > mutableTabs.size) {
            throw IndexOutOfBoundsException("Index $insertionIndex is out of bounds for tab list of size ${mutableTabs.size}")
        }

        mutableTabs.add(insertionIndex, newTab)


        if(isIncognito){
            _state.value = _state.value.copy(
                incognitoTabs = mutableTabs,
                incognitoTabIndex = insertionIndex,
                isIncognitoMode = true
            )
        }else{
            _state.value = _state.value.copy(
                regularTabs = mutableTabs,
                regularTabIndex = insertionIndex,
                isIncognitoMode = false
            )
        }

    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(
        url: String? = null,
        blank: Boolean = false,
        webView: WebView,
        isIncognito: Boolean,
        setTag: Boolean = true
    ): WebView{
        return webView.apply {
            if(url != null){
                loadUrl(url)
            } else{
                if(!blank){
                    loadUrl("https://google.com")
                }
            }

            id = View.generateViewId()
            if(setTag) {
                tag = UUID.randomUUID().toString()
            }
            layoutParams = ViewGroup.LayoutParams ( ViewGroup. LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)



            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            isDebugInspectorInfoEnabled = true
            isSaveEnabled  = true
            setWebContentsDebuggingEnabled(true)

            setDownloadListener { url, _, _, _, _ ->
                url?.let {
                    startDownload(
                        it
                    )
                }
            }
            setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                if (isDoneCounting) {
                    updateSearchResult(
                        webViewId = id,
                        totalMatches = numberOfMatches,
                        currentMatchIndex = activeMatchOrdinal,
                        isIncognito = isIncognito
                    )
                }
            }
            addJavascriptInterface(webAppInterface, "Android")

            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                if(_state.value.isDesktopMode) {
                    if (userAgentString != desktopString) {
                        userAgentString = desktopString
                    }
                }
            }
            configureWebView(isIncognito)

            webChromeClient = object : WebChromeClient() {



                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
                }


                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Timber.tag("WebView").d("Console Message: ${consoleMessage?.message()}")
                    return super.onConsoleMessage(consoleMessage)
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)

                    if(_state.value.currentTab?.webView?.id == id){
                        _state.value = _state.value.copy(
                            loadingPercentage = newProgress/100f
                        )
                    }

                    if(view?.url?.contains("youtube.com") == false){
                        updateCurrentTabUrl(
                            id = id,
                            url = view.url,
                            title = view.title,
                            isIncognito = isIncognito
                        )
                    }else{
                        view?.evaluateJavascript(
                            """
                              (function() {
                                    var videoId = window.location.href.split('v=')[1]?.split('&')[0];
                                    if (videoId) {
                                        return videoId;
                                    }
                                    return null;
                                })();
                            """.trim()
                        ) { videoID ->
                            if(videoID.toString() != "null"){
                                val newUrl = "https://m.youtube.com/watch?v=${videoID.replace("\"","")}"
                                updateCurrentTabUrl(
                                    id = id,
                                    url = newUrl,
                                    title = view.title,
                                    isIncognito = isIncognito
                                )
                                if(!isIncognito){
                                    historyRepository.addHistory(
                                        url = newUrl,
                                        title = view.title ?: "",
                                        timestamp = System.currentTimeMillis()
                                    )
                                }
                            }
                            if(view.tag == _state.value.currentTab?.webView?.tag){
                                onPaused()
                            }
                        }

                    }

                }

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    val newWebView = WebView(view?.context ?: context)
                    val parentTag = view?.tag.toString()
                    try {
                        val currentMatchIndex =  if(isIncognito) _state.value.incognitoTabIndex else _state.value.regularTabIndex
                        updatePreview(
                            index = currentMatchIndex,
                            isPrivate = isIncognito
                        )
                        addTab( webView = newWebView, index = currentMatchIndex + 1 , parentTag = parentTag, isPrivate = isIncognito)
                        currentTabOverlayUpdate(false)
                        val transport = resultMsg?.obj as? WebView.WebViewTransport
                        transport?.webView = newWebView
                        resultMsg?.sendToTarget()
                    } catch (e: IllegalArgumentException) {
                        return false
                    }
                    return true
                }


                override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                    scrollTo(0, 0)
                    removeAllViews()
                    addView(view, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    ))
                    currentTabViewUpdate(view,callback)
                    view.requestLayout()
                    view.invalidate()
                }

                override fun onHideCustomView() {
                    _state.value.currentTab?.view.let{
                        removeView(it)
                        currentTabViewUpdate(null, null)
                    }
                }
                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    // Handle JavaScript alert dialogs

                    AlertDialog.Builder(context)
                        .setTitle("Alert")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            result?.confirm()
                        }
                        .setCancelable(false)
                        .show()

                    return true
                }

                override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    AlertDialog.Builder(context)
                        .setTitle("Confirm")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            result?.confirm()
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ ->
                            result?.cancel()
                        }
                        .show()
                    return true
                }
            }

            webViewClient = object : WebViewClient() {


                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    if (isIncognito) {
                        view?.clearHistory()
                    }
                }

                override fun onPageFinished(webView: WebView?, url: String?) {

                    if(webView?.url?.contains("youtube.com") == true){
                        webView.youtubeAdBlockingScript()
                    }

                    if(!isIncognito){
                        if(webView?.url?.contains("google.com") == true){
                            webView.saveUserData()
                        }
                    }

                    if(webView != null){
                        updateCurrentTabUrl(
                            id = webView.id,
                            url = webView.url,
                            title = webView.title,
                            isIncognito = isIncognito
                        )
                    }

                    historyRepository.addHistory(
                        url = url ?: "",
                        title = webView?.title ?: "",
                        timestamp = System.currentTimeMillis()
                    )

                    super.onPageFinished(webView, url)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val pageUrl = request?.url?.toString() ?: return super.shouldOverrideUrlLoading(view, request)

                    if (pageUrl.startsWith("intent://")) {
                        handleIntentUrl(view, request.url.toString())
                        return true
                    }
                    return  super.shouldOverrideUrlLoading(view, request)
                }



                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {

                    val pageUrl = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

                    val isAds = runBlocking {
                        adBlocker.isAd(pageUrl)
                    }

                    return if (isAds) {
                        adBlocker.createEmptyResource()
                    } else {
                        super.shouldInterceptRequest(view, request)
                    }
                }

                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: RenderProcessGoneDetail?
                ): Boolean {
                    if (view?.id == id && detail?.didCrash() == true) {
                        val parent = view.parent as? ViewGroup
                        parent?.removeView(view)
                        val newWebView = WebView(view.context).apply {
                            id = view.id
                        }
                        parent?.addView(newWebView)

                        return true
                    }

                    return super.onRenderProcessGone(view, detail)
                }

                override fun onSafeBrowsingHit(
                    view: WebView?,
                    request: WebResourceRequest?,
                    threatType: Int,
                    callback: SafeBrowsingResponse?
                ) {
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_RESPONSE_BACK_TO_SAFETY)) {
                        callback?.backToSafety(true)
                        Toast.makeText(view?.context, "Unsafe web page blocked.", Toast.LENGTH_LONG).show()
                    }
                }

            }



        }
    }




    private fun startDownload(url: String) {
        viewModelScope.launch{
            val uuid = UUID.randomUUID().toString()
            val notificationId = Random.nextInt(200, 600)
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadRepository.startDownload(url, downloadsDir, uuid = uuid, notificationId = notificationId)
        }
    }

    private fun updateSearchResult(
        webViewId: Int,
        totalMatches: Int,
        currentMatchIndex: Int,
        isIncognito: Boolean = false
    ){
        if(_state.value.currentTab?.webView?.id == webViewId){
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val tabs = (if(isIncognito) _state.value.incognitoTabs else _state.value.regularTabs).toMutableList()
                    val currentTabIndex = if(isIncognito) _state.value.incognitoTabIndex else _state.value.regularTabIndex
                    val currentTabState = _state.value.currentTab

                    val newCurrentMatchIndex = if(totalMatches == 0){
                        0
                    } else{
                        currentMatchIndex + 1
                    }

                    val updatedTab = currentTabState?.copy(
                        totalSearchMatches = totalMatches,
                        currentSearchMatchIndex = newCurrentMatchIndex
                    )


                    updatedTab?.let {
                        tabs.removeAt(currentTabIndex)
                        tabs.add(currentTabIndex, it)

                        if(isIncognito){
                            _state.value = _state.value.copy(
                                incognitoTabs = tabs
                            )
                        }else{
                            _state.value = _state.value.copy(
                                regularTabs = tabs
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateCurrentTabUrl(id: Int, url: String?, title: String?, isIncognito: Boolean = false){
        val currentTab = _state.value.currentTab
        if(currentTab?.webView?.id == id && url != currentTab.url){
            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    val tabs = (if(isIncognito) _state.value.incognitoTabs else _state.value.regularTabs).toMutableList()
                    val currentTabIndex = if(isIncognito) _state.value.incognitoTabIndex else _state.value.regularTabIndex

                    val updatedTab = currentTab.copy(
                        url = url,
                        title = title,
                    )


                    updatedTab.let {
                        tabs.removeAt(currentTabIndex)
                        tabs.add(currentTabIndex, it)

                        if(isIncognito){
                            _state.value = _state.value.copy(
                                incognitoTabs = tabs
                            )
                        }else{
                            _state.value = _state.value.copy(
                                regularTabs = tabs
                            )
                        }
                    }
                }
            }
        }
    }

    fun updatePreview(bitmap: ImageBitmap? = null, index: Int? = null, isPrivate: Boolean? = null){
        val isIncognito = isPrivate ?: _state.value.isIncognitoMode
        val tab = _state.value.currentTab
         viewModelScope.launch {
             val currentIndex = if(isIncognito) _state.value.incognitoTabIndex else _state.value.regularTabIndex
             val overlay = tab?.showOverlay

             val imageBitmap = bitmap ?: tab?.webView.captureVisiblePartAndNavigate()

             if(imageBitmap != null){
                 val updatedTabs = (if(isIncognito) _state.value.incognitoTabs else _state.value.regularTabs).toMutableList()
                 updatedTabs[index ?: currentIndex] = updatedTabs[ index ?: currentIndex].copy(preview = imageBitmap)
                 if(isIncognito){
                     _state.value = _state.value.copy(
                         incognitoTabs = updatedTabs
                     )
                 }else{
                     _state.value = _state.value.copy(
                         regularTabs = updatedTabs
                     )
                 }

             }
             if(!isIncognito){
                 tabStateRepository.updateSingleTab(
                     webView = tab?.webView ?: return@launch,
                     index = index ?: currentIndex,
                     overlay = overlay ?: false,
                     imageBitmap = imageBitmap
                 )
             }
         }
    }



    fun switchToTab(index: Int) {
        val isIncognito = _state.value.isIncognitoMode
        if(isIncognito){
            if (index in _state.value.incognitoTabs.indices) {
                _state.value = _state.value.copy(incognitoTabIndex = index)
            }
        } else{
            if (index in _state.value.regularTabs.indices) {
                _state.value = _state.value.copy(regularTabIndex = index)
            }
        }
    }

    fun reloadCurrentTab() {
        _state.value.currentTab?.webView?.reload()
    }

    fun goBackInCurrentTab() {
        _state.value.currentTab?.let { tab ->
            val webView = tab.webView
            if (webView.canGoBack()) {
                if(tab.showOverlay == true){
                    currentTabOverlayUpdate(false)
                }else{
                    webView.goBack()
                    if(!webView.canGoBack()){
                        currentTabOverlayUpdate(true)
                    }
                }
            }else{
                if(tab.hasParent){
                    closeCurrentTab()
                }
            }
        }
    }

    fun goForwardInCurrentTab() {
        _state.value.currentTab?.webView.let { webView ->
            if (webView != null) {
                if (webView.canGoForward()) {
                    webView.goForward()
                    currentTabOverlayUpdate(false)
                }
            }
        }
    }


    fun currentTabOverlayUpdate(boolean: Boolean){
        val isIncognito = _state.value.isIncognitoMode
        val currentMatchIndex = _state.value.currentMatchIndex
        Log.d("incognito", "currentTabOverlayUpdate: $currentMatchIndex")
        val updatedTabs = (if(isIncognito) _state.value.incognitoTabs else _state.value.regularTabs).toMutableList()
        updatedTabs[currentMatchIndex] = updatedTabs[currentMatchIndex].copy(showOverlay = boolean)

        if(isIncognito){
            _state.value = _state.value.copy(
                incognitoTabs = updatedTabs
            )
        }else{
            _state.value = _state.value.copy(
                regularTabs = updatedTabs
            )
        }
    }

    private fun currentTabViewUpdate(view: View? = null, callback: CustomViewCallback? = null){
        val updatedTabs = _state.value.currentTabList.toMutableList()
        updatedTabs[_state.value.currentMatchIndex] = updatedTabs[_state.value.currentMatchIndex].copy(view = view, callback = callback)

        if(_state.value.isIncognitoMode){
            _state.value = _state.value.copy(
                incognitoTabs = updatedTabs
            )
        }else{
            _state.value = _state.value.copy(
                regularTabs = updatedTabs
            )
        }
    }


    private fun closeCurrentTab() {
        if (_state.value.currentTabList.isNotEmpty()) {
            val updatedTabs = _state.value.currentTabList.toMutableList()
            updatedTabs.removeAt(_state.value.currentMatchIndex)

            val newIndex = when {
                updatedTabs.isEmpty() -> 0
                _state.value.currentMatchIndex >= updatedTabs.size -> updatedTabs.size - 1
                else -> _state.value.currentMatchIndex
            }

            if(_state.value.isIncognitoMode){
                _state.value = _state.value.copy(
                    incognitoTabs = updatedTabs,
                    incognitoTabIndex = newIndex
                )

            }else{
                _state.value = _state.value.copy(
                    regularTabs = updatedTabs,
                    regularTabIndex = newIndex
                )
            }

            viewModelScope.launch {
                delay( 200)
                if(!_state.value.isIncognitoMode){
                    if(updatedTabs.isEmpty()){
                        println("newTabAdded")
                        addTab()
                    }
                }
            }
        }
    }


    private fun loadUrlInCurrentTab(url: String){
        _state.value.currentTab?.webView?.loadUrl(url)
    }


    fun closeTab(index: Int) {
        viewModelScope.launch {
            if (_state.value.currentTabList.isNotEmpty()) {
                tabStateRepository.deleteTabById(_state.value.currentTabList[index].webView.tag.toString())
                if(index == _state.value.currentMatchIndex){
                    closeCurrentTab()
                } else{
                    val updatedTabs = _state.value.currentTabList.toMutableList()
                    val currentTabIndex = _state.value.currentMatchIndex

                    if(index in updatedTabs.indices){
                        updatedTabs.removeAt(index)
                        val updatedIndex = currentTabIndex - if (index < currentTabIndex) 1 else 0


                        if(_state.value.isIncognitoMode){
                            _state.value = _state.value.copy(
                                incognitoTabs = updatedTabs,
                                incognitoTabIndex = updatedIndex
                            )
                        } else{
                            _state.value = _state.value.copy(
                                regularTabs = updatedTabs,
                                regularTabIndex = updatedIndex
                            )
                        }
                    }
                }

            }
        }
    }

    fun search(query: String) {
        if(query.isEmpty()){
            return
        }
        loadUrlInCurrentTab(formatUrl(query))
        currentTabOverlayUpdate(false)
    }

    private fun formatUrl(query: String): String {
        return when {
            query.startsWith("http://") || query.startsWith("https://") -> query
            query.startsWith("www.") -> "https://$query"
            else -> "https://www.google.com/search?q=$query"
        }
    }

    fun searchInWebView(searchQuery: String){
        if (searchQuery.isNotEmpty()) {
            _state.value.currentTab?.webView?.findAllAsync(searchQuery)
        } else {
            _state.value.currentTab?.webView?.clearMatches()
        }
    }

    fun toggleFindInPage(){
        val value = _state.value.isSearching
        _state.value = _state.value.copy(isSearching = !value)
    }

    fun upPressedInWebView(){
        _state.value.currentTab?.webView?.findNext(false)
    }

    fun downPressedInWebView(){
        _state.value.currentTab?.webView?.findNext(true)
    }

    fun toggleDesktopMode(){
        _state.value = _state.value.copy(isDesktopMode = !_state.value.isDesktopMode)
        val isDesktopMode = _state.value.isDesktopMode
        viewModelScope.launch {
                _state.value.currentTabList.forEach {
                    it.webView.settings.apply {
                        if(isDesktopMode) {
                            if (userAgentString != desktopString) {
                                userAgentString = desktopString
                            }
                        }else{
                            if (userAgentString != mobileString.value) {
                                userAgentString = mobileString.value
                            }
                    }
                }
            }

            withContext(Dispatchers.IO){
                delay(700)
            }
            reloadCurrentTab()
        }

    }



    private fun handleIntentUrl(view: WebView?, url: String) {
        try {
            callIntent(url)
        } catch (e: Exception) {
            println("Exception: $e")
            val packageName = url.substringAfter("package=").substringBefore(";")
            if (packageName.isNotEmpty() && !packageName.contains("vending", true)) {
                view?.loadUrl("https://play.google.com/store/apps/details?id=$packageName")
            }
        }
    }

    private fun callIntent(url: String){
        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun onPaused() {
        val tab = _state.value.regularTabs[_state.value.regularTabIndex]
        val bundle = Bundle()
        val webView = tab.webView
        val index = _state.value.regularTabIndex
        val overlay = tab.showOverlay
        webView.saveState(bundle)
        val title = webView.title ?: ""
        val cacheDir = context.filesDir.absolutePath
        val webViewTag = webView.tag.toString()
        bundleStatePreference.setCurrentTabTag(webViewTag)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val work1 = async { bundleStatePreference.updateWebViewState(bundle, webViewTag) }
                    val work2 = async {
                        tabStateRepository.updateCurrentTabState(
                            index = index,
                            title = title,
                            cacheDir = cacheDir,
                            webViewTag = webViewTag,
                            overlay = overlay ?: false
                        )
                    }
                    awaitAll(work1, work2)
                }
            } catch (e: Exception) {
                Timber.tag("BrowserViewModel").e(e, "Error updating webView or tab state")
            }
        }


    }


    override fun onCleared() {
        super.onCleared()
        onPaused()
    }



    fun clearAllCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }


}

