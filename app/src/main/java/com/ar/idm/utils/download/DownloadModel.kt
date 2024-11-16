package com.ar.idm.utils.download

import com.ar.idm.utils.function.asFileSize
import com.ar.idm.utils.function.calculateDownloadSpeed
import com.ar.idm.utils.function.calculateTimeRemaining
import java.util.Date

enum class FileType {
    IMAGE,
    VIDEO,
    AUDIO,
    PDF,
    DOCUMENT,
    SPREADSHEET,
    COMPRESSED,
    TEXT,
    OTHER
}


data class DownloadFile(
    val uuid: String,
    val name: String,
    val uri: String,
    val fileType: FileType,
    var downloadProgress: Float = 0f,
    val totalSize: Long = 0L,
    val startTime: Long = 0L,
    val downloadStatus: String = "",
    val isPaused: Boolean = false
){

    val currentSize: Long
        get() = (downloadProgress * totalSize).toLong()

    val currentSizeString: String
        get() = currentSize.asFileSize()

    val totalSizeString: String
        get() = totalSize.asFileSize()

    val progressPercentage: String
        get() = (downloadProgress * 100).toInt().toString() + "%"


    val timeRemaining: String
        get() = calculateTimeRemaining(currentSize, totalSize, startTime)

    val downloadSpeed: String
        get() = calculateDownloadSpeed(currentSize, startTime).asFileSize() + "/sec"

    val date get() = Date(startTime)

    val isDownloading get() = downloadProgress < 1f


}

data class DownloadFilesState(
    val downloadFiles: List<DownloadFile> = emptyList()
)



