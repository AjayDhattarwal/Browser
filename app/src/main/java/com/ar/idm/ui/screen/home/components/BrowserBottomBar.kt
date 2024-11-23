
package com.ar.idm.ui.screen.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.ar.idm.domain.model.BrowserState
import com.ar.idm.ui.navigation.AppDestination
import kotlinx.coroutines.flow.StateFlow

@Composable
fun BrowserBottomBar(
    modifier: Modifier = Modifier,
    browserState: StateFlow<BrowserState>,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit,
    onShare: () -> Unit,
    tabIcon: @Composable () -> Unit,
    onNavigate: (AppDestination) -> Unit,
    newTab: (isPrivate: Boolean) -> Unit,
    findInPage: () -> Unit,
    desktopMode: () -> Unit,
    updatePreview: (ImageBitmap?) -> Unit,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var clearDataDialogExpanded by remember { mutableStateOf(false) }
    val state by browserState.collectAsState()

    val showOverlay by remember {
        derivedStateOf {
            state.currentTab?.showOverlay ?: false
        }
    }
    val tabCount by remember {
        derivedStateOf {
            state.tabCount
        }
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        IconButton(onClick = onNavigateForward) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Forward"
            )
        }

        IconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share"
            )
        }

        tabIcon()

        IconButton(onClick = {dropdownExpanded = !dropdownExpanded }) {

            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Bookmark",
                modifier = Modifier
            )

        }
    }
    if (dropdownExpanded) {
        HomeMenuBottomSheet(
            browserState = browserState,
            onNavigate = onNavigate,
            onDismiss = { dropdownExpanded = false },
            clearBrowsingData = {
                if (state.currentTab?.webView != null) {
                    if (!showOverlay) updatePreview(null)
                }
                clearDataDialogExpanded = !clearDataDialogExpanded
            },
            newTab = newTab,
            desktopMode = {
                dropdownExpanded = false
                desktopMode()
            },
            findInPage = {
                findInPage()
                dropdownExpanded = false
            },
        )
    }

    if(clearDataDialogExpanded){
        ClearBrowseDataDialog(
            onDismissRequest = {clearDataDialogExpanded = false},
            image = state.currentTab?.preview,
            onClearData = {
                clearDataDialogExpanded = false
                TODO()
            },
            lastVisitedUrl = state.currentTab?.url.toString(),
            tabCount = tabCount,
        )
    }
}

