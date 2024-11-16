package com.ar.idm.viewmodel

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.idm.utils.download.DownloadRepository
import com.ar.idm.utils.download.DownloadFilesState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class DownloadViewModel(
    private val downloadRepository: DownloadRepository
) : ViewModel() {



    val downloadState: StateFlow<DownloadFilesState> = downloadRepository.downloadState



    fun togglePauseResume(uuid: String) {
        viewModelScope.launch {
            downloadRepository.pauseOrResumeDownload(uuid)
        }
    }

    fun cancelDownload(uuid: String) {
        downloadRepository.cancelDownload(uuid)
    }

}


