@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)

package com.ar.idm.ui.screen.home.components

import android.content.ClipDescription
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ar.idm.R
import com.ar.idm.domain.model.BrowserState
import com.ar.idm.ui.components.CommonCardRowFrame
import com.ar.idm.utils.function.extractUrl
import com.ar.idm.ui.navigation.AppDestination
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
    homeView: (Boolean) -> Unit
) {

    val state by browserState.collectAsState()
    val url by remember { derivedStateOf{
        state.currentTab?.url ?: ""
    } }

    val title by remember { derivedStateOf{
        state.currentTab?.title
    }}

    val favIconUrl by remember { derivedStateOf{
        state.currentTab?.favIconUrl
    }}


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
//                            true
                        }, target = callback
                    )
                    .clickable { onNavigate(AppDestination.Search(
                        url = url,
                        title = title,
                        favIconUrl = favIconUrl
                    )) },
                leadingIcon = {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(
                            painter = painterResource(if(state.isIncognitoMode) R.drawable.ic_incognito else R.drawable.ic_info),
                            contentDescription = "Lock",
                            tint = Color.Gray
                        )
                    }
                },
                content = {
                    Text(
                        text = url,
                        style =  MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
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
    }
}