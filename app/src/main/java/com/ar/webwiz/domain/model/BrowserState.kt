package com.ar.webwiz.domain.model

import okhttp3.internal.connection.RouteSelector

data class BrowserState(

    val regularTabs: List<TabState> = emptyList(),
    val regularTabIndex: Int = 0,

    val incognitoTabs: List<TabState> = emptyList(),
    val incognitoTabIndex: Int = 0,

    val loadingPercentage: Float = 0f,
    val isSearching: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isIncognitoMode: Boolean = false,

    val currentTabTag: String = "null",
    val isDSelectionEnabled: Boolean = false
) {
    val isLoading: Boolean
        get() = loadingPercentage < 1f

    val currentMatchIndex: Int
        get() = when (isIncognitoMode) {
            true -> incognitoTabIndex
            false -> regularTabIndex
        }

    val currentTab: TabState?
        get() = when (isIncognitoMode) {
            true -> incognitoTabs.getOrNull(incognitoTabIndex)
            false -> regularTabs.getOrNull(regularTabIndex)
        }

    val canGoBack: Boolean
        get() = currentTab?.webView?.canGoBack() == true


    val currentTabList: List<TabState>
        get() = when (isIncognitoMode) {
            true -> incognitoTabs
            false -> regularTabs
        }

    val tabCount: Int
        get() = when (isIncognitoMode) {
            true -> incognitoTabs.size
            false -> regularTabs.size
        }


}
