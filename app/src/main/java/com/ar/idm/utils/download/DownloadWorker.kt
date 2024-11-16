package com.ar.idm.utils.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ar.idm.utils.download.DownloadService.Companion.ACTION_CANCEL_DOWNLOAD
import com.ar.idm.utils.download.DownloadService.Companion.ACTION_DOWNLOAD_COMPLETE
import com.ar.idm.utils.download.DownloadService.Companion.ACTION_START_DOWNLOAD
import com.ar.idm.utils.download.DownloadService.Companion.ACTION_UPDATE_PROGRESS
import com.ar.idm.data.local.preferences.PausePreferencesManager
import com.ar.idm.data.local.roomdatabase.downloadDb.DownloadFileEntity
import com.ar.idm.data.local.roomdatabase.downloadDb.DownloadRange
import com.ar.idm.utils.function.asFileSize
import com.ar.idm.utils.function.asFileType
import com.ar.idm.utils.function.calculateTimeRemaining
import com.ar.idm.utils.function.createUniqueFile
import com.ar.idm.utils.function.supportResume
import io.ktor.util.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "DownloadWorker"

@OptIn(InternalAPI::class)
class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val context = applicationContext

    private val downloadRepository: DownloadRepository = KoinJavaComponent.getKoin().get()

    private val NOTIFICATION_ID = inputData.getInt("notificationId", -1)

    private val pausePreferencesManager = PausePreferencesManager(context)

    private var downloadJob = Job()

    private var fileName = "unknown"
    private var fileSize = 0L
    private var startTime = 0L
    private val updateIntervalMs = 1500
    private var lastUpdateTime = System.currentTimeMillis()
    private var totalBytesDownloaded = 0L
    private var mimeType: String? = null
    private var downloadLocation = ""
    private var listOfRanges: MutableList<DownloadRange> = mutableListOf()
    private var isCancelledByPause: Boolean = false
    private val tagUuid = inputData.getString("uuid_key")  ?: ""

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO + downloadJob) {
            try {


                val urlString = inputData.getString("url") ?: return@withContext Result.failure()
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val numberOfThreads = inputData.getInt("threads", 1)


                val startDownloadIntent = Intent(context, DownloadService::class.java).apply {
                    action = ACTION_START_DOWNLOAD
                    putExtra("notificationId", NOTIFICATION_ID)
                    putExtra("title", "File Download")
                    putExtra("content", "Download in progress")
                    putExtra("worker_uuid_key", tagUuid)
                }

                context.startService(startDownloadIntent)

                downloadFile(urlString, numberOfThreads, downloadsDir)

                if (!isStopped) {
                    setProgressAsync(
                        workDataOf(
                            "progress" to 100
                        )
                    )
                    downloadCompletedNotification()
                    downloadRepository.deleteDownloadingFile(tagUuid)
                    Result.success(workDataOf(
                        "downloadStatus" to "success"
                    ))
                } else {
                    val file = File(downloadsDir, fileName)
                    if(file.exists()){
                        file.delete()
                    }
                    Result.failure(workDataOf(
                        "downloadStatus" to "Failure"
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if(!isCancelledByPause){
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, fileName)
                    if(file.exists()){
                        file.delete()
                    }
                }
                println(e.message)
                cancelNotification()
                Result.failure(workDataOf(
                    "downloadStatus" to if(isCancelledByPause) "paused" else "url Error"
                ))
            }
        }
    }

    private suspend fun downloadFile(
        urlString: String,
        numberOfThreads: Int,
        downloadsDir: File,
    ) {
        withContext(Dispatchers.IO) {
            val resumedData = downloadRepository.getDownloadingFileByUuid(tagUuid)
            downloadRepository.deleteDownloadingFile(tagUuid)
            downloadRepository.observeDownload(tagUuid)

            startTime = System.currentTimeMillis()

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 20000
            connection.readTimeout = 20000

            fileSize = connection.contentLengthLong
            mimeType = connection.contentType
            val contentDisposition = connection.getHeaderField("Content-Disposition")

            val newFileName = contentDisposition?.let {
                it.split("=")[1].trim().removeSurrounding("\"")
            } ?: Uri.parse(urlString).lastPathSegment ?: "downloaded_file"


            val outputFile = if(resumedData != null){
                File(resumedData.downloadLocation)
            } else{
                File(downloadsDir, newFileName).createUniqueFile()
            }


            fileName = outputFile.name
            downloadLocation = outputFile.path


            if (!isStopped) {
                setProgressAsync(
                    workDataOf(
                        "title" to outputFile.name,
                        "totalSize" to fileSize,
                        "uri" to url.toString(),
                        "fileType" to mimeType.asFileType().toString(),
                        "startTime" to startTime,
                        "downloadStatus" to "Started"
                    )
                )
                println("Data updated")
            }



            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", outputFile)

            try {
                val partSize = fileSize / numberOfThreads
                val ranges = if(resumedData != null){
                    resumedData.pausedRanges?.map {
                        totalBytesDownloaded += it.newStart - it.initialStart + 1
                        Log.d( "downloadFile: ", "$totalBytesDownloaded")
                        it.newStart ..it.end
                    }
                }else{
                    List(numberOfThreads) { index ->
                        val start = index * partSize
                        val end = if (index == numberOfThreads - 1) fileSize - 1 else (start + partSize - 1)
                        start..end
                    }

                }

                coroutineScope {
                    val jobs = ranges?.mapIndexed { index, range ->
                        async {
                            println("Downloading part ${index + 1}/${ranges.size} (bytes ${range.first}-${range.last})")
                            try {
                                val partConnection = url.openConnection() as HttpURLConnection
                                partConnection.setRequestProperty("Range", "bytes=${range.first}-${range.last}")
                                partConnection.connect()

                                if (partConnection.responseCode != HttpURLConnection.HTTP_PARTIAL) {
                                    Timber.e("Error: Response code " + partConnection.responseCode + " for thread " + index)
                                    return@async
                                }

                                val isPauseAble = partConnection.supportResume()
                                partConnection.inputStream.use { inputStream ->
                                    context.contentResolver.openFileDescriptor(uri, "rw")
                                        ?.use { pfd ->
                                            FileOutputStream(pfd.fileDescriptor).use { fos ->
                                                fos.channel.position(range.first)
                                                writeToFile(
                                                    fileOutputStream = fos,
                                                    index = index,
                                                    range = range,
                                                    inputStream = inputStream,
                                                    fileSize = fileSize,
                                                    isPauseAble = isPauseAble,
                                                )
                                            }
                                        }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    jobs?.awaitAll()
                }
            } catch (e : Exception){
                e.printStackTrace()
            }
        }
    }

    suspend fun writeToFile(
        fileOutputStream: FileOutputStream,
        index: Int,
        range: LongRange,
        inputStream: InputStream,
        fileSize: Long,
        isPauseAble: Boolean
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytesRead: Int

        var totalBytesReadInCurrentRange = 0L

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            fileOutputStream.write(buffer, 0, bytesRead)
            totalBytesReadInCurrentRange += bytesRead

            totalBytesDownloaded += bytesRead
            val progress =
                (totalBytesDownloaded.toDouble() / fileSize) * 100
            updateProgress(progress, totalBytesDownloaded)

            handlePauseOrStop(isPauseAble, index, range, range.first + totalBytesReadInCurrentRange)
        }
    }





    private suspend fun handlePauseOrStop(isPauseAble: Boolean, index: Int, range: LongRange, newStart: Long) {
        if (isStopped) {
            cancelNotification()
            downloadJob.cancel()
            return
        }
        if (isPaused()) {
            updateNotification("", 0, "Download paused", true)
            listOfRanges.add(DownloadRange(threadIndex = index, initialStart = range.first, end = range.last, newStart =  newStart))

            if(isPauseAble){
                var timer = 0
                while (isPaused()) {
                    delay(500)
                    if (isStopped) {
                        cancelNotification()
                        downloadJob.cancel()
                        break
                    }
                    timer += 1
                    if (timer > 10) {  //120 == 1 min
                        isCancelledByPause = true
                        insertDownloadFile(listOfRanges, isPauseAble)
                        downloadJob.cancel()
                    }
                }
            }else{
                while (isPaused() ) {
                    delay(500)
                    if (isStopped) {
                        cancelNotification()
                        downloadJob.cancel()
                        break
                    }
                }
            }
        }
    }

    private suspend fun insertDownloadFile(pausedRanges: List<DownloadRange>? = null, isPauseAble: Boolean) {
        val downloadFile = DownloadFileEntity(
            title = fileName,
            downloadUrl = inputData.getString("url") ?: "",
            fileSize = fileSize,
            downloadLocation = downloadLocation,
            fileType = mimeType ?: "other",
            isPauseAble = isPauseAble,
            pausedRanges = pausedRanges,
            notificationId = NOTIFICATION_ID,
            uuid = inputData.getString("uuid_key") ?: "",
            currentDownloadedSize = totalBytesDownloaded,
            time = startTime
        )
        downloadRepository.insertDownloadingFile(downloadFile)
    }



    private  fun updateProgress(progress: Double, currentSize: Long) {
        val progressPercent = progress.toInt()
        try {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastUpdateTime >= updateIntervalMs && progress < 100) {
                if (isStopped) {
                    Timber.tag("DownloadWorker").d("Cancellation detected, stopping download")
                    downloadJob.cancel()
                    return
                }
                lastUpdateTime = currentTime
                val workInfo = WorkManager.getInstance(applicationContext).getWorkInfoById(id).get()


                if (workInfo?.state == WorkInfo.State.RUNNING) {
                    setProgressAsync(
                        workDataOf(
                            "progress" to progress.toFloat(),
                            "currentSize" to currentSize
                        )
                    )

                    val content =
                        "${currentSize.asFileSize()} of ${fileSize.asFileSize()} downloaded"
                    val subTitle = calculateTimeRemaining(currentSize, fileSize, startTime)

                    updateNotification(subTitle, progressPercent, content)


                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error fetching work info:  ${e.message}")
        }

    }


    private fun isPaused():Boolean {
        return pausePreferencesManager.isPaused(tagUuid)
    }


    private fun updateNotification(subTitle: String, progressPercent: Int, content: String, isPaused: Boolean = false){
        val startDownloadIntent = Intent(context, DownloadService::class.java).apply {
            action = ACTION_UPDATE_PROGRESS
            putExtra("notificationId", NOTIFICATION_ID)
            putExtra("title", fileName)
            putExtra("content", content)
            putExtra("subTitle", subTitle)
            putExtra("progress", progressPercent)
            putExtra("isPaused", isPaused)
            putExtra("worker_uuid_key", inputData.getString(inputData.getString("uuid_key") ?: ""))
        }
        context.startService(startDownloadIntent)
    }


    private fun cancelNotification(){
        if(!isCancelledByPause){
            val startDownloadIntent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
                putExtra("notificationId", NOTIFICATION_ID)
            }
            context.startService(startDownloadIntent)
        }
    }

    private fun downloadCompletedNotification(){
        val startDownloadIntent = Intent(context, DownloadService::class.java).apply {
            action = ACTION_DOWNLOAD_COMPLETE
            putExtra("notificationId", NOTIFICATION_ID)
            putExtra("title", fileName)

        }
        context.startService(startDownloadIntent)
    }


}

