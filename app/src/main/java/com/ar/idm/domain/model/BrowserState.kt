package com.ar.idm.domain.model

data class BrowserState(

    val regularTabs: List<TabState> = emptyList(),
    val regularTabIndex: Int = 0,

    //incognito
    val incognitoTabs: List<TabState> = emptyList(),
    val incognitoTabIndex: Int = 0,

    val loadingPercentage: Float = 0f,
    val isSearching: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isIncognitoMode: Boolean = false,
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
