package com.ar.idm.utils.download

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ar.idm.data.local.preferences.PausePreferencesManager
import com.ar.idm.data.local.roomdatabase.downloadDb.DownloadDatabase
import com.ar.idm.data.local.roomdatabase.downloadDb.DownloadFileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File



class DownloadRepository(private val context: Context, downloadDatabase: DownloadDatabase) {

    private val downloadFileDao = downloadDatabase.downloadFileDao()
    private val downloadJobs = mutableMapOf<String, Job>()

    private val _downloadFilesState = MutableStateFlow(DownloadFilesState())
    val downloadState: StateFlow<DownloadFilesState> = _downloadFilesState.asStateFlow()

    private val workManager = WorkManager.getInstance(context)

    private val pausePreferencesManager = PausePreferencesManager(context)

    fun startDownload(
        url: String,
        outputPath: File,
        thread: Int = 4,
        uuid: String,
        notificationId: Int
    ) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .build()


        val data = Data.Builder()
            .putString("url", url)
            .putInt("threads", thread)
            .putInt("notificationId", notificationId)
            .putString("uuid_key", uuid)
            .build()

        val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .addTag(uuid)
            .build()

        workManager.enqueue(downloadWorkRequest)
    }

    fun cancelDownload(uuid: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(uuid)
        _downloadFilesState.value = _downloadFilesState.value.copy(
            downloadFiles = _downloadFilesState.value.downloadFiles.filterNot { it.uuid == uuid }
        )
        println(_downloadFilesState.value)
        println(downloadState.value)
    }

    suspend fun pauseOrResumeDownload(downloadId: String) {
        val resumeData = getDownloadingFileByUuid(downloadId)
        if(resumeData != null){
            pausePreferencesManager.resumeWorker(downloadId)
            startDownload(
                url = resumeData.downloadUrl,
                outputPath = File(resumeData.downloadLocation),
                uuid = resumeData.uuid,
                notificationId = resumeData.notificationId
            )
        }else{
            pausePreferencesManager.togglePause(downloadId)
        }

        updateDownloadFile(
            uuid = downloadId
        ){
            it.copy(isPaused = pausePreferencesManager.isPaused(downloadId))
        }
    }




    suspend fun observeDownload(uuid: String, observe: Boolean = true) {
        Log.d("DownloadRepository", "observeDownload called with uuid: $uuid")

        if (downloadJobs.containsKey(uuid)) {
            downloadJobs[uuid]?.cancel()
        }

        if (!observe) {
            return
        }

        val job = CoroutineScope(Dispatchers.IO).launch {
            val updatedFile = observeFileStatus(uuid)

            _downloadFilesState.value = _downloadFilesState.value.copy(
                downloadFiles = _downloadFilesState.value.downloadFiles
                    .filterNot { it.uuid == uuid }
                    .plus(updatedFile)
            )

            println(_downloadFilesState.value.downloadFiles)

            coroutineScope {
                launch {
                    observeDownloadProgress(uuid).collect { progress ->
                        updateDownloadFile(uuid) { file ->
                            println(progress)
                            if(progress > 0f){
                                file.copy(
                                    downloadProgress = progress/100f,
                                )
                            } else{
                                file
                            }
                        }
                    }
                }

                launch {
                    observeDownloadStatus(uuid).collect { downloadStatus ->
                        updateDownloadFile(uuid) { file ->
                            println(downloadStatus)
                            if (downloadStatus == "pause") {
                                file.copy(
                                    downloadStatus = downloadStatus,
                                    isPaused = true
                                )
                            } else {
                                file.copy(
                                    downloadProgress = 100f,
                                    downloadStatus = downloadStatus
                                )
                            }
                        }
                    }
                }
            }
        }
        downloadJobs[uuid] = job
    }


    private fun updateDownloadFile(
        uuid: String,
        mutableList: MutableList<DownloadFile> = _downloadFilesState.value.downloadFiles.toMutableList(),
        updateFunc: (DownloadFile) -> DownloadFile
    ) {
        val updatedFiles = mutableList.map { file ->
            if (file.uuid == uuid) {
                updateFunc(file)
            } else {
                file
            }
        }

        _downloadFilesState.value = _downloadFilesState.value.copy(
            downloadFiles = updatedFiles
        )
    }



    ////////// Observer for download status //////////



    private fun observeDownloadStatus(uuid: String): Flow<String> {
        return WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(uuid)
            .mapNotNull { workInfos ->
                workInfos.firstOrNull()?.let { workInfo ->
                    val downloadStatus = workInfo.outputData.getString("downloadStatus")
                    val isSuccess = workInfo.state.isFinished

                    if (isSuccess && downloadStatus != null) {
                        downloadStatus
                    } else {
                        null
                    }
                }
            }.distinctUntilChanged()
    }


    private fun observeDownloadProgress(uuid: String): Flow<Float> {
        return WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(uuid)
            .map { workInfos ->
                workInfos.firstOrNull()?.let { workInfo ->
                    val progressValue = workInfo.progress.getFloat("progress", 0f)
                    if (progressValue > 0f) {
                        progressValue
                    } else {
                        0f
                    }
                } ?: 0f
            }.distinctUntilChanged()
    }



    private suspend fun observeFileStatus(uuid: String): DownloadFile {
        return WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(uuid)
            .mapNotNull { workInfos ->
                workInfos.firstOrNull()?.let { workInfoItem ->
                    val title = workInfoItem.progress.getString("title") ?: ""
                    val totalSize = workInfoItem.progress.getLong("totalSize", 1)
                    val uri = workInfoItem.progress.getString("uri") ?: ""
                    val fileType = workInfoItem.progress.getString("fileType")?.let { FileType.valueOf(it) } ?: FileType.OTHER
                    val startTime = workInfoItem.progress.getLong("startTime", 0)
                    val downloadStatus = workInfoItem.progress.getString("downloadStatus") ?: ""

                    if (title.isNotEmpty() && totalSize > 0) {
                        DownloadFile(
                            uuid = uuid,
                            name = title,
                            totalSize = totalSize,
                            uri = uri,
                            fileType = fileType,
                            startTime = startTime,
                            downloadStatus = downloadStatus
                        )
                    } else {
                        null
                    }
                }
            }
            .first { downloadFile ->
                downloadFile.name.isNotEmpty() && downloadFile.totalSize > 0
            }
    }




    suspend fun insertDownloadingFile(downloadFile: DownloadFileEntity) {
        withContext(Dispatchers.IO){
            downloadFileDao.insert(downloadFile = downloadFile)
        }
    }

    private suspend fun fetchDownloadingFiles(): List<DownloadFileEntity> {
        val downloadFiles = downloadFileDao.getAllDownloadFiles()
        return downloadFiles
    }

    suspend fun getDownloadingFileByUuid(uuid: String): DownloadFileEntity? {
        return downloadFileDao.getDownloadFileByUuid(uuid)
    }

    suspend fun deleteDownloadingFile(uuid: String) {
        downloadFileDao.deleteDownloadingFile(uuid)
    }



}

