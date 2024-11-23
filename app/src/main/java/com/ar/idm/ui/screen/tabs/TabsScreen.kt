@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalSharedTransitionApi::class)

package com.ar.idm.ui.screen.tabs

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace

import coil.compose.rememberAsyncImagePainter
import com.ar.idm.R
import com.ar.idm.domain.model.BrowserState
import com.ar.idm.domain.model.TabState
import com.ar.idm.ui.components.SwipeToDeleteContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TabsScreen(
    pagerState: PagerState,
    browserState: StateFlow<BrowserState>,
    newTab: () -> Unit,
    closeTab: (Int) -> Unit,
    switchTab: (Int) -> Unit,
    updateTabState: (Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigateBack: () -> Unit
)  = trace("TabsScreen") {

    val state by browserState.collectAsState()

    val isCurrentTabNull by remember { derivedStateOf { state.currentTab == null } }

    LaunchedEffect(Unit) {
        if(state.isIncognitoMode){
            pagerState.scrollToPage(1)

        }else{
            pagerState.scrollToPage(0)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            updateTabState(page != 0)
        }
    }


    val scope = rememberCoroutineScope()



    BackHandler {
        if(isCurrentTabNull){
            scope.launch {
                pagerState.animateScrollToPage(0)
            }
        }else{
            navigateBack()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = newTab,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Tab"
                    )
                    Text(
                        text = "New Tab",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                TabRow(
                    selectedTabIndex = 0,
                    modifier = Modifier.width(80.dp).align(Alignment.Center),
                    indicator = {},
                    divider =  {}
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_tabs),
                                contentDescription = "regular tab"
                            )
                        },
                        selectedContentColor =  MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_incognito),
                                contentDescription = "incognito"
                            )
                        },
                        selectedContentColor =  MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = {index -> index},
                beyondViewportPageCount = 1,
            ) { page ->
                when(page){
                    0 -> {
                        LazyStaggeredGrid(
                            browserState = browserState,
                            isIncognito = false,
                            closeTab = closeTab,
                            switchTab = switchTab,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                    1 -> {
                        LazyStaggeredGrid(
                            browserState = browserState,
                            isIncognito = true,
                            closeTab = closeTab,
                            switchTab = switchTab,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun LazyStaggeredGrid(
    browserState: StateFlow<BrowserState>,
    isIncognito: Boolean,
    closeTab: (Int) -> Unit,
    switchTab: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
){
    val state by browserState.collectAsState()

    val list by remember {
        derivedStateOf {
            if(isIncognito){ state.incognitoTabs } else { state.regularTabs }
        }
    }
    val currentTabIndex by remember {
        derivedStateOf {
            if(isIncognito){ state.incognitoTabIndex } else { state.regularTabIndex }
        }
    }


        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(180.dp),
            contentPadding = PaddingValues(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("Grid_of_tabs")
        ) {
            itemsIndexed(list, key = { _, tabState -> tabState.tag }) { index, tabState ->
                val color = if(currentTabIndex == index){
                    MaterialTheme.colorScheme.primaryContainer
                }else{
                    MaterialTheme.colorScheme.surfaceVariant
                }

                SwipeToDeleteContainer(
                    modifier = Modifier.animateItem(),
                    item = tabState,
                    onRemove = { closeTab(index) },
                ){
                    TabCard(
                        color = color,
                        tabState = tabState,
                        index = index,
                        onClick = { switchTab(index) },
                        closeTab = { closeTab(index) },
                        isIncognito = isIncognito,
                        sharedTransitionScope =  sharedTransitionScope,
                        animatedVisibilityScope =  animatedVisibilityScope
                    )
                }
            }
        }

}


@Composable
fun TabCard(
    tabState: TabState,
    color: Color,
    index: Int,
    onClick: () -> Unit,
    closeTab: () -> Unit,
    isIncognito: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
)  = trace("TabItemTag") {

    val key by remember { mutableStateOf ("shareBounds${index}_$isIncognito" ) }

    val favicon by remember { derivedStateOf {
        tabState.favIconUrl
    } }

    val title by remember {
        derivedStateOf {
            tabState.title
        }
    }
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .sharedBounds(
                    rememberSharedContentState(key = key),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = color
            ),
            border = BorderStroke(1.7.dp, color),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(true) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Image(
                        painter = rememberAsyncImagePainter(favicon),
                        contentDescription = "Favicon",
                        modifier = Modifier
                            .padding(start = 10.dp, end = 5.dp)
                            .size(16.dp)
                    )


                    Text(
                        text = title ?: "Untitled",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = closeTab,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier
                                .padding(end = 10.dp)
                        )
                    }

                }
                trace("ImagePlaceholder") {

                    Box(
                        modifier = Modifier
                            .height(230.dp)
                    ) {
                        Image(
                            bitmap = tabState.preview
                                ?: Bitmap.createBitmap(
                                    300,
                                    500,
                                    Bitmap.Config.ARGB_8888
                                ).apply {
                                    eraseColor(
                                        android.graphics.Color.WHITE
                                    )
                                }.asImageBitmap(),
                            contentDescription = "Preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            contentScale = ContentScale.FillWidth
                        )


                    }
                }
            }
        }
    }

}







