@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.ar.webwiz.ui.screen.menu.download

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ar.webwiz.utils.download.DownloadFilesState
import com.ar.webwiz.ui.screen.menu.download.components.DownloadFileItem
import com.ar.webwiz.ui.navigation.AppDestination
import kotlinx.coroutines.flow.StateFlow

@Composable
fun DownloadScreen(
    downloadState: StateFlow<DownloadFilesState>,
    navigate: (AppDestination) -> Unit,
    navigateBack: () -> Unit,
    togglePauseResumeDownload: (String) -> Unit,
    onCancelDownload: (String) -> Unit
){
    val downloadFiles by downloadState.collectAsState()
    val downloadingFiles = downloadFiles.downloadFiles

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Download",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                },
                actions = {
                    IconButton(
                        onClick = {navigate(AppDestination.DownloadSetting)}
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = navigateBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            stickyHeader(key = "header") {
                Text(
                    text = "Using 0.00Kb of 5.80Gb",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                )
                Spacer(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(0.2f))
                        .height(1.dp)
                        .fillMaxWidth()

                )
            }

                items(downloadingFiles, key = { it.uuid }) { file ->
                    DownloadFileItem(
                        file = file,
                        onCancelDownload = onCancelDownload,
                        onPauseResumeDownload = togglePauseResumeDownload,
                        showProgress = file.isDownloading,
                        isPaused = file.isPaused
                    )
                }

        }

    }
}
