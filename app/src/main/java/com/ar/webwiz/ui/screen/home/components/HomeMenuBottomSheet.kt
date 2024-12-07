@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.ar.webwiz.ui.screen.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ar.webwiz.R
import com.ar.webwiz.domain.model.BrowserState
import com.ar.webwiz.ui.components.CommonCardRowFrame
import com.ar.webwiz.ui.components.CommonIconFrame
import com.ar.webwiz.ui.navigation.AppDestination
import com.ar.webwiz.viewmodel.BookmarkViewModel
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeMenuBottomSheet(
    browserState: StateFlow<BrowserState>,
    onNavigate: (AppDestination) -> Unit,
    onDismiss: () -> Unit,
    clearBrowsingData: () -> Unit,
    newTab: (isPrivate: Boolean) -> Unit,
    desktopMode: () -> Unit,
    findInPage: () -> Unit,
    bookmarkViewModel: BookmarkViewModel = koinViewModel()
) {

    val sheetState = rememberModalBottomSheetState()
    val state by browserState.collectAsState()

    val isDesktopMode by remember { derivedStateOf { state.isDesktopMode } }


    val bookmarkState by bookmarkViewModel.bookmarks.collectAsState()
    val isCurrentBookmark by remember {
        derivedStateOf{
            bookmarkState.any{
                it.second.any { bookmark ->
                    bookmark.url == state.currentTab?.url
                }
            }
        }
    }


    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {

        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ){
            Column {

                LazyVerticalStaggeredGrid (
                    columns = StaggeredGridCells.Adaptive(170.dp),
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalItemSpacing = 14.dp

                ){
                    item {
                        CommonCardRowFrame(
                            title = "New Tab",
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_add_box),
                                    contentDescription = "New Tab"
                                )
                            },
                            onClick = {
                                onDismiss()
                                newTab(false)
                            },
                            modifier = Modifier.height(50.dp)
                        )
                    }

                    item{
                        CommonCardRowFrame(
                            title = "New Incognito Tab",
                            onClick = {
                                onDismiss()
                                newTab(true)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_incognito),
                                    contentDescription = "New Incognito Tab"
                                )
                            },
                            modifier = Modifier.height(50.dp)
                        )
                    }

                    item {
                        CommonCardRowFrame(
                            title = "History",
                            onClick = {
                                onDismiss()
                                onNavigate(AppDestination.History)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_history),
                                    contentDescription = "History"
                                )
                            },
                            modifier = Modifier.height(50.dp)
                        )
                    }

                    item {
                        CommonCardRowFrame(
                            title = "Clear Browsing Data",
                            onClick = {
                                onDismiss()
                                clearBrowsingData()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear Browsing Data"
                                )
                            },
                            modifier = Modifier.height(50.dp)
                        )
                    }
                    item {
                        CommonCardRowFrame(
                            title = "Download",
                            onClick = {
                                onDismiss()
                                onNavigate(AppDestination.Downloads)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_download_done),
                                    contentDescription = "Download"
                                )
                            },
                            modifier = Modifier.height(50.dp)
                        )
                    }

                    item {
                        CommonCardRowFrame(
                            title = "Bookmarks",
                            onClick = {
                                onDismiss()
                                onNavigate(AppDestination.Bookmarks)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_bookmark),
                                    contentDescription = "Bookmarks"
                                )
                            },
                            modifier = Modifier.height(50.dp)
                        )
                    }

                    item {
                        CommonCardRowFrame(
                            title = "Settings",
                            onClick = {
                                onDismiss()
                                onNavigate(AppDestination.Settings)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            modifier = Modifier.height(50.dp)
                        )
                    }

                    item{
                        CommonCardRowFrame(
                            title = "Help & Feedback",
                            onClick = {
                                onDismiss()
                                onNavigate(AppDestination.HelpAndFeedback)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_help),
                                    contentDescription = "Settings"
                                )
                            },
                            modifier = Modifier.height(50.dp)
                        )
                    }


                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    Card(
                        modifier = Modifier.width(180.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors().copy(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            CommonIconFrame(
                                icon = if(isCurrentBookmark) R.drawable.ic_bookmark_fill else R.drawable.ic_bookmark,
                                onClick = remember {
                                    {
                                        bookmarkViewModel.toggleBookmark(
                                            state.currentTab?.url,
                                            state.currentTab?.title
                                        )
                                    }
                                }
                            )
                            CommonIconFrame(
                                icon = R.drawable.ic_find_in_page,
                                onClick = findInPage
                            )

                            CommonIconFrame(
                                icon = if(isDesktopMode) R.drawable.ic_desktop_fill else  R.drawable.ic_desktop,
                                onClick = desktopMode
                            )
                        }

                    }
                }
            }

        }

    }
}
