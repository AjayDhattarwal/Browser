package com.ar.idm.ui.screen.menu.download.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ar.idm.R
import com.ar.idm.utils.download.DownloadFile
import com.ar.idm.utils.download.FileType
import com.ar.idm.utils.function.asFileSize


@Composable
fun DownloadFileItem(
    file: DownloadFile,
    onCancelDownload: (String) -> Unit,
    onPauseResumeDownload: (String) -> Unit,
    showProgress: Boolean,
    isPaused: Boolean = false
) {

    val fileState = file
    println(fileState)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !showProgress,
                onClick = {},
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileThumbnail(file = fileState)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = fileState.name, style = MaterialTheme.typography.bodySmall)

            if (showProgress) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { fileState.downloadProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${fileState.currentSizeString}/${fileState.totalSizeString} (${fileState.downloadSpeed})",
                    style = MaterialTheme.typography.labelSmall
                )
            } else {
                Text(
                    text = fileState.totalSize.asFileSize(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if(showProgress){
            IconButton(onClick = { onPauseResumeDownload(fileState.uuid) }) {
                Icon(
                    painter = painterResource(if (isPaused) R.drawable.ic_resume else R.drawable.ic_pause),
                    contentDescription = if (isPaused) "Resume download" else "Pause download"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { onCancelDownload(fileState.uuid) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_cancel),
                    contentDescription = "Cancel download"
                )
            }
        } else{
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "more"
                )
            }
        }

    }
}

@Composable
fun FileThumbnail(file: DownloadFile) {

    val thumbnail = when (file.fileType) {
        FileType.IMAGE -> R.drawable.ic_image
        FileType.VIDEO -> R.drawable.ic_video
        FileType.PDF -> R.drawable.ic_pdf
        else -> R.drawable.ic_file
    }

    Icon(
        painter = painterResource(thumbnail),
        contentDescription = "File thumbnail",
        modifier = Modifier.size(48.dp)
    )
}

