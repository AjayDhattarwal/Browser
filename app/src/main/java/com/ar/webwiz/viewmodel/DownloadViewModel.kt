package com.ar.webwiz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.webwiz.utils.download.DownloadRepository
import com.ar.webwiz.utils.download.DownloadFilesState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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


