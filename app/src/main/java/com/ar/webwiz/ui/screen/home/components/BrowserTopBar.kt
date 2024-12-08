@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)

package com.ar.webwiz.ui.screen.home.components

import android.content.ClipDescription
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ar.webwiz.R
import com.ar.webwiz.domain.model.BrowserState
import com.ar.webwiz.ui.components.CommonCardRowFrame
import com.ar.webwiz.utils.function.extractUrl
import com.ar.webwiz.ui.navigation.AppDestination
import com.ar.webwiz.utils.function.extractDomain
import kotlinx.coroutines.flow.StateFlow

@Stable
@Composable
fun BrowserTopBar(
    modifier: Modifier,
    browserState: StateFlow<BrowserState>,
    onNavigate: (AppDestination) -> Unit,
    onRefresh: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onSearch: (String) -> Unit,
    homeView: (Boolean) -> Unit,
    toggleDistractionSelect: () -> Unit,
    hideDistractions: () -> Unit,
    toggleReaderMode: () -> Unit
) {

    val state by browserState.collectAsState()

    val isDistractionEnabled by remember { derivedStateOf { state.isDSelectionEnabled } }

    val isReaderModeEnabled by remember { derivedStateOf { state.isReaderMode } }

    val url by remember { derivedStateOf{
        state.currentTab?.url ?: ""
    } }

    val domain by remember { derivedStateOf{extractDomain(url) ?: ""}}

    val title by remember { derivedStateOf{
        state.currentTab?.title
    }}

    val favIconUrl by remember { derivedStateOf{
        state.currentTab?.favIconUrl
    }}

    var expandedTopMenu by remember { mutableStateOf(false) }


    Row(
        modifier = modifier.padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        IconButton(onClick = { homeView(true)}) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home"
            )
        }
        val callback = remember {
            object : DragAndDropTarget {
                override fun onDrop(event: DragAndDropEvent): Boolean {
                    val text = event.toAndroidDragEvent().clipData.getItemAt(0).text.toString()
                    println(text)
                    val extractedUrl = extractUrl(text)
                    onSearch(extractedUrl ?: text)
                    return true
                }
            }
        }

        with(sharedTransitionScope){
            CommonCardRowFrame(
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "searchBar"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    .height(40.dp)
                    .weight(1f)
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { event ->
                            event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                        }, target = callback
                    )
                    .clickable { onNavigate(AppDestination.Search(
                        url = url,
                        title = title,
                        favIconUrl = favIconUrl
                    )) },
                leadingIcon = {
                    if(!isDistractionEnabled) {
                        IconButton(
                            onClick = { expandedTopMenu = true },
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(
                                painter = painterResource(if(isReaderModeEnabled) R.drawable.ic_article_fill else R.drawable.page_info),
                                contentDescription = "menu",
                                tint = if(isReaderModeEnabled) Color.Gray else Color.DarkGray.copy(0.8f)
                            )
                        }
                    }
                },
                content = {

                        AnimatedVisibility(
                            isDistractionEnabled,
                            enter = slideInHorizontally() + fadeIn(),
                            exit = slideOutHorizontally()+ fadeOut()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Select items to hide",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )

                                TextButton(
                                    colors = ButtonDefaults.buttonColors().copy(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(0.7f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(2.dp),
                                    onClick = { toggleDistractionSelect() },
                                    modifier = Modifier.height(25.dp)
                                ) {
                                    Text("Cancel", style = MaterialTheme.typography.labelSmall)
                                }

                                Spacer(Modifier.width(3.dp))

                                TextButton(
                                    colors = ButtonDefaults.buttonColors().copy(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(0.7f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(2.dp),
                                    onClick = { hideDistractions() },
                                    modifier = Modifier.height(25.dp)
                                ) {
                                    Text("Done", style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(Modifier.width(4.dp))
                            }
                        }

                        AnimatedVisibility(
                            !isDistractionEnabled,
                            enter = slideInHorizontally{it/2} + fadeIn(),
                            exit = slideOutHorizontally{it/2}+ fadeOut()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(if (state.isIncognitoMode) R.drawable.ic_incognito else R.drawable.ic_info),
                                    contentDescription = "Lock",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = domain,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                        }


                },
                trailingIcon = {
                    if(!isDistractionEnabled){
                        Spacer(Modifier.width(20.dp))
                    }
                },
                shape = RoundedCornerShape(50)
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Search"
            )
        }

        DropdownMenu(
            expanded = expandedTopMenu,
            onDismissRequest = { expandedTopMenu = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    toggleDistractionSelect()
                    expandedTopMenu = false
                },
                text = { Text(text = "Hide Distraction") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_visibility_off),
                        contentDescription = "Hide Distraction"
                    )
                }
            )

            DropdownMenuItem(
                onClick = {
                    toggleReaderMode()
                    expandedTopMenu = false
                },
                text = { Text(text = if(isReaderModeEnabled) "Hide Article View " else "Show Article View") },
                leadingIcon = {
                    Icon(
                        painter = painterResource( if(isReaderModeEnabled) R.drawable.ic_article_fill else R.drawable.ic_article),
                        contentDescription = "Reader Mode"
                    )
                }
            )
        }
    }
}

