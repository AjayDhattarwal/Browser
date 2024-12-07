package com.ar.webwiz.utils.download

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.ar.webwiz.utils.function.asFileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong

class DownloadManager(private val context: Context) {

    private val TAG = "DownloadManager"
    val downloadJob = Job() // Create a Job for managing cancellation
    val scope = CoroutineScope(Dispatchers.IO + downloadJob)

    suspend fun downloadFile(
        uuid: String?,
        urlString: String,
        numberOfThreads: Int,
        downloadsDir: File,
        returnFile: (DownloadFile) -> Unit,
        onProgressUpdate: (Double, Long) -> Unit,
    ) {

        try {
            withContext(Dispatchers.IO) {

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 20000
                connection.readTimeout = 20000

                println("Connecting to URL: $urlString")

                val fileSize = connection.contentLengthLong
                val mimeType = connection.contentType
                val contentDisposition = connection.getHeaderField("Content-Disposition")

                println("File size: $fileSize , mime-type: $mimeType ")

                val fileName = contentDisposition?.let {
                    Regex("filename=\"?([^\";]+)\"?").find(it)?.groups?.get(1)?.value
                } ?: Uri.parse(urlString).lastPathSegment.toString()


                val downloadFile = DownloadFile(
                    uuid = uuid.toString(),
                    name = fileName,
                    totalSize = fileSize,
                    uri = url.toString(),
                    fileType = mimeType.asFileType(),
                    startTime = System.currentTimeMillis()
                )

                returnFile(downloadFile)


                val partSize = fileSize / numberOfThreads
                val totalProgress = AtomicLong(0)

                val file = File(downloadsDir, fileName)


                file.parentFile?.mkdirs()
                if (!file.exists()) {
                    file.createNewFile()
                }

                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val jobs = List(numberOfThreads) { threadIndex ->
                        async {
                            try {
                                val partStart = threadIndex * partSize
                                val partEnd = if (threadIndex == numberOfThreads - 1) fileSize - 1 else (threadIndex + 1) * partSize - 1

                                Timber.d("Thread $threadIndex: Downloading bytes $partStart to $partEnd")

                                val partConnection = url.openConnection() as HttpURLConnection
                                partConnection.setRequestProperty("Range", "bytes=$partStart-$partEnd")
                                partConnection.connect()

                                if (partConnection.responseCode != HttpURLConnection.HTTP_PARTIAL) {
                                    Timber.e("Error: Response code " + partConnection.responseCode + " for thread " + threadIndex)
                                    return@async
                                }

                                partConnection.inputStream.use { inputStream ->
                                    context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                                        FileOutputStream(pfd.fileDescriptor).use { fos ->
                                            fos.channel.position(partStart)
                                            writeToFile(
                                                fos,
                                                inputStream,
                                                totalProgress,
                                                fileSize,
                                                onProgressUpdate
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.tag(TAG).e("Error during download $threadIndex: ${e.message}")
                            }
                        }
                    }

                    jobs.awaitAll()
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error during download: ${e.message}")
        }
    }

    private fun writeToFile(
        fileOutputStream: FileOutputStream,
        inputStream: InputStream,
        totalProgress: AtomicLong,
        fileSize: Long,
        onProgress: (Double,Long) -> Unit
    ) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        var totalBytesRead: Long = 0
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            fileOutputStream.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead

            val currentProgress = totalProgress.addAndGet(bytesRead.toLong())
            val progress = (currentProgress.toDouble()/ fileSize) * 100.0
            onProgress(progress, currentProgress)
        }
    }

    fun stopDownload() {
        Timber.tag(TAG).d("Stopping download")
        downloadJob.cancel()
    }

}