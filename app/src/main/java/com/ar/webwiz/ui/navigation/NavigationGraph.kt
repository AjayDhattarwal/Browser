@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalSharedTransitionApi::class,
    ExperimentalSharedTransitionApi::class, ExperimentalSharedTransitionApi::class,
    ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class,
)

package com.ar.webwiz.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.ar.webwiz.ui.screen.home.HomeScreen
import com.ar.webwiz.ui.screen.menu.help.HelpAndFeedbackScreen
import com.ar.webwiz.ui.screen.menu.bookmarks.BookmarkScreen
import com.ar.webwiz.ui.screen.search.SearchScreen
import com.ar.webwiz.ui.screen.tabs.TabsScreen
import com.ar.webwiz.ui.screen.menu.download.DownloadScreen
import com.ar.webwiz.ui.screen.menu.history.HistoryScreen
import com.ar.webwiz.ui.screen.menu.settings.SettingsScreen
import com.ar.webwiz.ui.screen.search.CameraScreen
import com.ar.webwiz.viewmodel.BrowserViewModel
import com.ar.webwiz.viewmodel.DownloadViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavigationGraph(browserViewModel: BrowserViewModel = koinViewModel()) {

    val idmNavController =  rememberIDMNavController()
    val browserState = browserViewModel.state

    val downloadViewModel: DownloadViewModel = koinViewModel()
    val downloadState = downloadViewModel.downloadState
    val pagerState = rememberPagerState { 2 }


    val tabGridState = rememberLazyGridState()

    SharedTransitionLayout {
        NavHost(
            navController = idmNavController.navController,
            startDestination = AppDestination.Home,
            modifier = Modifier.semantics {
                testTagsAsResourceId = true
            },
        ) {

            composable<AppDestination.Home> {
                HomeScreen(
                    browserState = browserState,
                    navigateTabBack =  browserViewModel::goBackInCurrentTab,
                    onTabSelection = remember{
                        { idmNavController.navigate(AppDestination.Tabs) }
                    },
                    navigateForward = browserViewModel::goForwardInCurrentTab,
                    onRefresh = browserViewModel::reloadCurrentTab,
                    updatePreview = browserViewModel::updatePreview,
                    navigate = idmNavController::navigate,
                    newTab = {browserViewModel.addTab(isPrivate = it)},
                    desktopMode = browserViewModel::toggleDesktopMode,
                    onSearch = browserViewModel::search,
                    homeView = browserViewModel::currentTabOverlayUpdate,
                    inWebViewSearch = browserViewModel::searchInWebView,
                    setFindInPage = browserViewModel::toggleFindInPage,
                    upPressedInWebView = browserViewModel::upPressedInWebView,
                    downPressedInWebView = browserViewModel::downPressedInWebView,
                    hideDistractions = browserViewModel::hideDistractions,
                    toggleDistractionSelect = browserViewModel::toggleDistractionSelect,
                    animatedVisibilityScope = this@composable,
                    sharedTransitionScope = this@SharedTransitionLayout
                )


            }
            composable<AppDestination.Tabs> {
                TabsScreen(
                    pagerState = pagerState,
                    browserState = browserState,
                    newTab = {
                        browserViewModel.addTab()
                        idmNavController.navigate(AppDestination.Home)
                    },
                    closeTab = browserViewModel::closeTab,
                    switchTab = {
                        browserViewModel.switchToTab(it)
                        idmNavController.navigate(AppDestination.Home)
                    },
                    updateTabState = browserViewModel::updateTabState,
                    animatedVisibilityScope = this@composable,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    navigateBack = idmNavController::upPress
                )
            }

            composable<AppDestination.Search>(
                deepLinks = listOf(navDeepLink { uriPattern = "browser://browser/search/{query}" })
            ) { backStackEntry ->
                val data: AppDestination.Search = backStackEntry.toRoute()
                val query = backStackEntry.arguments?.getString("query")

                SearchScreen(
                    data = data.copy(query = query),
                    onSearch = {
                        browserViewModel.search(it)
                        idmNavController.upPress()
                    },
                    navigate = idmNavController::navigate,
                    navigateBack = idmNavController::upPress,
                    animatedVisibilityScope = this@composable,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }

            composable<AppDestination.CameraScreen> {
                CameraScreen(
                    navigateBack = idmNavController::upPress,
                    navigate = idmNavController::navigate,
                    searchImage = {
//                        idmNavController.navigate(AppDestination.Home)
                    }
                )
            }

            composable<AppDestination.Downloads>{
                DownloadScreen(
                    downloadState = downloadState,
                    navigate = idmNavController::navigate,
                    navigateBack = idmNavController::upPress,
                    togglePauseResumeDownload = downloadViewModel::togglePauseResume,
                    onCancelDownload = downloadViewModel::cancelDownload,
                )
            }

            composable<AppDestination.History>{
                HistoryScreen(
                    navigateBack = idmNavController::upPress,
                    navigate = idmNavController::navigate
                )
            }

            composable<AppDestination.Bookmarks>{
                BookmarkScreen(
                    browserState = browserState,
                    navigateBack = idmNavController::upPress,
                    navigate = idmNavController::navigate
                )
            }

            composable<AppDestination.Settings>{
                SettingsScreen(
                    navigate = idmNavController::navigate,
                    navigateBack = idmNavController::upPress
                )
            }

            composable<AppDestination.HelpAndFeedback> {
                HelpAndFeedbackScreen(
                    onSendFeedback = {},
                    onContactSupport = {},
                    navigateBack = idmNavController::upPress,
                    navigate = idmNavController::navigate
                )
            }
        }
    }
}




