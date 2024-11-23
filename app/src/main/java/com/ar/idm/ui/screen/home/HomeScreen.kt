@file:OptIn( ExperimentalSharedTransitionApi::class)

package com.ar.idm.ui.screen.home

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.trace
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ar.idm.domain.model.BrowserState
import com.ar.idm.ui.screen.home.components.BrowserBottomBar
import com.ar.idm.ui.screen.home.components.BrowserTopBar
import com.ar.idm.ui.navigation.AppDestination
import com.ar.idm.ui.screen.home.components.BrowserView
import com.ar.idm.ui.screen.home.components.InWebViewSearch
import com.ar.idm.ui.screen.home.components.OverlayHomeComposable
import com.ar.idm.utils.internalFunction.findActivity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    browserState: StateFlow<BrowserState>,
    navigateTabBack: () -> Unit,
    navigateForward: () -> Unit,
    onRefresh: () -> Unit,
    navigate: (AppDestination) -> Unit,
    onTabSelection: () -> Unit,
    updatePreview: (ImageBitmap?) -> Unit,
    newTab: (isPrivate: Boolean) -> Unit,
    desktopMode: () -> Unit,
    onSearch: (String) -> Unit,
    homeView: (Boolean) -> Unit,
    inWebViewSearch: (String) -> Unit,
    setFindInPage: () -> Unit,
    upPressedInWebView: () -> Unit,
    downPressedInWebView: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
) = trace("HomeScreen") {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    val state by browserState.collectAsState()

    val fullScreenCallBack by remember { derivedStateOf {  state.currentTab?.callback } }

    val currentTabIsNotNull by remember { derivedStateOf { state.currentTab?.webView != null } }

    val canGoBack by remember { derivedStateOf { state.canGoBack || state.currentTab?.hasParent == true } }

    val sharedTransitionKey by remember { derivedStateOf { "shareBounds${state.currentMatchIndex}_${state.isIncognitoMode}" } }

    val showOverlay by remember {
        derivedStateOf {
            state.currentTab?.showOverlay == true
        }
    }

    val tabsCount by remember {
        derivedStateOf {
            state.tabCount.toString()
        }
    }


    val isFindingInPage by remember { derivedStateOf { state.isSearching } }


    var overlapBitmap: ImageBitmap? by remember { mutableStateOf(null) }

    val isFullScreen by remember { derivedStateOf { state.currentTab?.isFullScreen == true } }


    val activity = remember { context.findActivity() }
    val window = remember { activity.window }

    LaunchedEffect(isFullScreen) {
        trace("FullScreen"){
            if (isFullScreen) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowCompat.setDecorFitsSystemWindows(window, true)
                WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    BackHandler(enabled = isFullScreen || canGoBack) {
        if (isFullScreen) {
            fullScreenCallBack?.onCustomViewHidden()
        } else if (canGoBack) {
            navigateTabBack()
        }
    }

    with(sharedTransitionScope) {

        Scaffold(
            modifier = Modifier
                .testTag("Home")
                .fillMaxSize()
                .sharedBounds(
                    rememberSharedContentState(key = sharedTransitionKey),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
            containerColor =  MaterialTheme.colorScheme.surface,
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    if(!showOverlay){
                        AnimatedVisibility(
                            visible = !isFullScreen,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically(),
                            modifier = Modifier
                        ) {
                            if(isFindingInPage){
                                InWebViewSearch(
                                    browserState = browserState,
                                    onSearch = inWebViewSearch,
                                    onDismiss = setFindInPage,
                                    upPressed = upPressedInWebView,
                                    downPressed = downPressedInWebView
                                )

                            }else{
                                BrowserTopBar(
                                    modifier = Modifier,
                                    browserState = browserState,
                                    onNavigate = navigate,
                                    onRefresh = onRefresh,
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    onSearch = onSearch,
                                    homeView = homeView
                                )
                            }
                        }

                        BrowserView(
                            browserState = browserState,
                            modifier = Modifier
                        )
                    } else{
                        trace("overLayDraw"){
                            Box(
                                modifier = Modifier
                                    .graphicsLayer()
                                    .drawWithContent {
                                        graphicsLayer.record {
                                            this@drawWithContent.drawContent()
                                        }
                                        drawLayer(graphicsLayer)
                                        coroutineScope.launch {
                                            overlapBitmap = graphicsLayer.toImageBitmap()
                                        }
                                    }


                            ) {
                                OverlayHomeComposable(
                                    modifier = Modifier.fillMaxSize(),
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    onNavigate = navigate,
                                    onSearch = onSearch
                                )

                            }
                        }

                    }
                }

                AnimatedVisibility(
                    visible = !isFullScreen,
                    enter = fadeIn() + slideInVertically{  it / 2  },
                    exit = fadeOut() + slideOutVertically{  it / 2  },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    BrowserBottomBar(
                        browserState =  browserState ,
                        onNavigateBack = navigateTabBack,
                        onNavigateForward = navigateForward,
                        onShare = { },
                        tabIcon = {
                            IconButton(
                                onClick = {},
                            ) {
                                Box(
                                    modifier = Modifier
                                        .testTag("TabButtonBox")
                                        .clip(RoundedCornerShape(15))
                                        .border(
                                            BorderStroke(
                                                1.5.dp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(15)
                                        )
                                        .clickable {
                                            if (currentTabIsNotNull) {
                                                val imageBitmap =
                                                    if (showOverlay) overlapBitmap else null
                                                updatePreview(imageBitmap)
                                            }
                                            onTabSelection()
                                        },

                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tabsCount,
                                        textAlign = TextAlign.Center,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                                    )
                                }
                            }
                        },
                        onNavigate = navigate,
                        newTab = newTab,
                        desktopMode = desktopMode,
                        updatePreview = updatePreview,
                        findInPage = setFindInPage
                    )
                }

            }
        }
    }
}


