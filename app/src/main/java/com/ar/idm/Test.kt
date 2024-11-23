//
//
//
//@file:OptIn(InternalAPI::class)
//
//package com.ar.idm
//
//import com.ar.idm.ui.function.formatTime
//import io.ktor.client.HttpClient
//import io.ktor.client.engine.cio.CIO
//import io.ktor.client.plugins.HttpTimeout
//import io.ktor.client.request.get
//import io.ktor.client.request.head
//import io.ktor.client.request.header
//import io.ktor.client.request.prepareGet
//import io.ktor.client.statement.HttpResponse
//import io.ktor.http.HttpHeaders
//import io.ktor.http.contentLength
//import io.ktor.http.contentType
//import io.ktor.util.InternalAPI
//import io.ktor.utils.io.jvm.javaio.toInputStream
//import io.ktor.utils.io.readAvailable
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import java.io.RandomAccessFile
//
//
//fun main(): Unit = runBlocking {
//    val fileUrl =
//        "https://pub-b9aa7706b08a48708172cdf88fcedb75.r2.dev/My.Fault.2023.1080p.WEB-DL.Hindi.AAC.5.1-English.ESub.x264-HDHub4u.Tv.mkv"
//    val outputPath = "Sarkaru.mkv"
//    try {
//        downloadFileConcurrently(fileUrl, outputPath, numThreads = 8) { progress ->
//            println("Download progress: $progress%")
//        }
//    } catch (e: IOException) {
//        println("Download failed: ${e.message}")
//    }
//}
//
//
//
//suspend fun downloadFileConcurrently(
//    url: String,
//    outputPath: String,
//    numThreads: Int = 8,
//    onProgress: (Float) -> Unit,
//) {
//    val client = HttpClient(CIO) {
//        install(HttpTimeout) {
//            requestTimeoutMillis = 200000
//            socketTimeoutMillis = 20000
//        }
//    }
//    val outputFile = RandomAccessFile(File(outputPath), "rw")
//    try {
//        val headResponse: HttpResponse = client.head(url)
//        val fileSize = headResponse.contentLength() ?: throw Exception("Couldn't determine file size")
//        println("File size: $fileSize bytes")
//
//        headResponse.contentType()
//
//        if (!headResponse.headers.contains(HttpHeaders.AcceptRanges, "bytes")) {
//            throw Exception("Server does not support range requests")
//        }
//        println("Server supports range requests.")
//
//        val partSize = fileSize / numThreads
//        val ranges = List(numThreads) { index ->
//            val start = index * partSize
//            val end = if (index == numThreads - 1) fileSize - 1 else (start + partSize - 1)
//            start..end
//        }
//
//        println("File divided into $numThreads parts.")
//
//
//        // Total bytes downloaded
//        var totalBytesDownloaded = 0L
//
//        val startTime = System.currentTimeMillis()
//
//        coroutineScope {
//            val jobs = ranges.mapIndexed { index, range ->
//                async {
//                    println("Downloading part ${index + 1}/${ranges.size} (bytes ${range.first}-${range.last})")
//                    val response = client.prepareGet(url) {
//                        header(HttpHeaders.Range, "bytes=${range.first}-${range.last}")
//                    }
//
//                    response.execute { responseBody ->
//                        val channel = responseBody.content
//
//                        outputFile.seek(range.first)
//                        val buffer = ByteArray(1024) // 1 KB buffer
//                        var totalBytesReadInCurrentRange = 0L
//
//                        // Track bytes left to download in the current range
//                        val bytesToReadInRange = range.last - range.first + 1
//
//                        while (totalBytesReadInCurrentRange < bytesToReadInRange) {
//                            // Determine how many bytes to read, ensuring we don't exceed the range boundary
//                            val bytesRemaining = bytesToReadInRange - totalBytesReadInCurrentRange
//                            val bytesRead = channel.readAvailable(
//                                buffer,
//                                0,
//                                minOf(buffer.size, bytesRemaining.toInt())
//                            )
//
//                            if (bytesRead == -1) break // End of stream
//
//                            outputFile.write(buffer, 0, bytesRead)
//                            totalBytesReadInCurrentRange += bytesRead
//                            totalBytesDownloaded += bytesRead
//
//                            // Report progress every 1% increase
//                            val progress = (totalBytesDownloaded.toFloat() / fileSize) * 100
//                            if (progress.toInt() % 1 == 0) { // Adjust this for less frequent updates
//                                onProgress(progress)
//                            }
//                        }
//                    }
//                }
//            }
//            jobs.awaitAll()
//        }
//
//        val endTime = System.currentTimeMillis()
//
//        val elapsedTime = endTime - startTime
//
//        print(formatTime(elapsedTime))
//
//        // Ensure final progress is reported after download completes
//        onProgress(100f)
//
//        println("All parts downloaded. Closing file.")
//    } catch (e: Exception) {
//        println("An error occurred: ${e.message}")
//    } finally {
//        outputFile.close()
//        client.close()
//        println("Download completed!")
//    }
//}


//@file:OptIn(InternalAPI::class)
//
//import io.ktor.client.*
//import io.ktor.client.call.body
//import io.ktor.client.engine.cio.*
//import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
//import io.ktor.client.plugins.logging.*
//import io.ktor.client.request.*
//import io.ktor.client.statement.bodyAsText
//import io.ktor.http.*
//import io.ktor.serialization.kotlinx.json.json
//import io.ktor.util.InternalAPI
//import kotlinx.coroutines.runBlocking
//import kotlinx.serialization.json.Json
//
//suspend fun fetchStateNews() {
//    val client = HttpClient(CIO){
//        install(ContentNegotiation) {
//            json(Json { ignoreUnknownKeys = true })
//        }
//    }
//    try {
//        val response = client.get("https://prod.bhaskarapi.com/api/1.0/web-backend/state-news/list") {
//            headers {
//                append("Cache-Control", "no-cache")
//                append("cid", "521")
//                append("x-aut-web-t", "420x66695ztde3qao6a69")
//            }
//        }
//
//        println("Response Status: ${response.status}")
//        println("Response: ${response.bodyAsText()}")
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//    } finally {
//        client.close()
//    }
//}
//
//suspend fun fetchSingleNews() {
//    val client = HttpClient(CIO){
//        install(ContentNegotiation) {
//            json(Json { ignoreUnknownKeys = true })
//        }
//    }
//    try {
//        val response = client.get("https://www.bhaskar.com/__api__/api/1.0/feed/story/filename/maharashtra-islam-history-explained-alauddin-khilji-vs-yadav-raja-mughal-aurangzeb-133851653") {
//            headers {
//                append("Accept-Language", "en-IN,en-GB;q=0.9,en;q=0.8")
//                append("dtyp", "web")
//                append("x-aut-web-t", "420x66695ztde3qao6a69")
//            }
//        }
//
//
//        println("Response Status: ${response.status}")
//        println("Response: ${response.bodyAsText()}")
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//    } finally {
//        client.close()
//    }
//}
//
//fun main() = runBlocking {
//    fetchSingleNews()
//}


// headline   https://feeds.intoday.in/tts/it_headlines_v2.json


//import java.io.File
@file:OptIn(InternalAPI::class)

import java.net.URL

//fun main() {
//    // Specify the input and output file paths
//    val inputFilePath = "/Users/ajaysingh/AndroidStudioProjects/IDM/app/src/main/assets/adsServerList.txt"
//    val outputFilePath = "output_unique.txt"
//
//    // Read all lines from the input file and filter out duplicates
//    val uniqueLines = File(inputFilePath)
//        .readLines()
//        .toSet() // Convert to a set to keep only unique lines
//        .sorted() // Optional: Sort the lines alphabetically
//
//    // Write the unique lines to the output file
//    File(outputFilePath).printWriter().use { writer ->
//        uniqueLines.forEach { line ->
//            writer.println(line)
//        }
//    }
//
//    println("Unique lines have been written to $outputFilePath")
//}


//import java.io.IOException
//import java.net.HttpURLConnection
//
//fun main() {
//    val url = "https://easylist-downloads.adblockplus.org/easylist-minified.txt"
//    val downloadedData = downloadFile(url)
//
//    // Process the downloaded data
//    val domains = mutableListOf<String>()
//    val blockScripts = mutableListOf<String>()
//
//    downloadedData.lines().forEach { line ->
//        when {
//            line.startsWith("||") || line.startsWith("|") || line.startsWith("@@") -> {
//                domains.add(line)
//            }
//            line.startsWith("$") || line.startsWith("-") -> {
//                blockScripts.add(line)
//            }
//            // You can add more conditions here to create more files based on other criteria
//        }
//    }
//
//    // Write to domains.txt
//    writeToFile("domains.txt", domains)
//    // Write to blockscript.txt
//    writeToFile("blockscript.txt", blockScripts)
//
//    // You can add more files based on different data types here
//}
//
//// Function to download file content from a URL
//fun downloadFile(fileUrl: String): String {
//    val url = URL(fileUrl)
//    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
//    return connection.inputStream.bufferedReader().use { it.readText() }
//}
//
//// Function to write data to a file
//fun writeToFile(fileName: String, data: List<String>) {
//    try {
//        File(fileName).printWriter().use { out ->
//            data.forEach { line ->
//                out.println(line)
//            }
//        }
//        println("Successfully wrote to $fileName")
//    } catch (e: IOException) {
//        println("An error occurred while writing to $fileName: ${e.message}")
//    }
//}


//test  ads file for 20 files


//fun main() {
//    // Specify the input and output file paths
//    val inputFilePath = "easylist.txt" // Replace with your input file path
//    val uniqueFilePath = "unique_values.txt"
//    val outputDirectory = "/Users/ajaysingh/AndroidStudioProjects/IDM/app/src/main/assets" // Directory to store the 20 output files
//
//    // Step 1: Read the input file and get unique lines
//    val uniqueLines = File(inputFilePath).readLines().toSet().sorted()
//
//    // Step 2: Write sorted unique lines to a unique value file
//    File(uniqueFilePath).writeText(uniqueLines.joinToString("\n"))
//
//    // Step 3: Divide sorted unique values into 20 files
//    val linesPerFile = (uniqueLines.size + 29) / 30 // Calculate how many lines per file
//    uniqueLines.chunked(linesPerFile).forEachIndexed { index, lines ->
//        val outputFilePath = "$outputDirectory/adBlockFile_${index + 1}.txt"
//        File(outputFilePath).writeText(lines.joinToString("\n"))
//    }
//
//    println("Sorted unique values written to $uniqueFilePath and divided into 20 files in $outputDirectory.")
//}
//




//import io.ktor.client.*
//import io.ktor.client.call.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//import io.ktor.http.*
//import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.client.plugins.logging.*
//import io.ktor.client.plugins.*
//import io.ktor.client.request.forms.*
//import io.ktor.client.plugins.cookies.*
//import kotlinx.coroutines.runBlocking
//import java.io.File
//
//suspend fun uploadImageToGoogleLens(file: File): HttpResponse {
//    val client = HttpClient(CIO) {
//        install(DefaultRequest) {
//            header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
//        }
//    }
//
//    return client.submitFormWithBinaryData(
//        url = "https://lens.google.com/v3/upload?hl=en-IN",
//        formData = formData {
//            append("encoded_image", file.readBytes(), Headers.build {
//                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
//                append(HttpHeaders.ContentType, "image/jpeg")
//            })
////            append("processed_image_dimensions", "239,148")
//        }
//    ).also {
//        println("Status: ${it.status}")
//        println("Response: ${it.bodyAsText()}")
//    }
//}
//
//fun main() {
//    runBlocking {
//        val file = File("image.jpeg")
//        uploadImageToGoogleLens(file)
//    }
//}
//

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.*
import io.ktor.http.ContentType.MultiPart.FormData
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.InternalAPI
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

suspend fun main() {
    val client = HttpClient(CIO){
        install(HttpTimeout){
            requestTimeoutMillis = 20000
            socketTimeoutMillis = 20000
            connectTimeoutMillis = 20000
        }
    }

    // Specify the file path correctly
    val filePath = "audio.mp3"
    val file = File(filePath)

    // Verify the file exists
    if (!file.exists()) {
        println("File does not exist at: $filePath")
        return
    }

    // Get the file size in bytes
    val fileSize = file.length()  // This gives the file size in bytes

    println("File size: $fileSize bytes")

//    try {
//        val response = client.submitFormWithBinaryData(
//            url = "https://api.doreso.com/humming", //humming //identify
//            formData = formData {
//                append("file", file.readBytes(), Headers.build {
//                    append(HttpHeaders.ContentType, ContentType.Audio.MPEG.toString())
//                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
//                })
//                append("sample_size", fileSize.toString())  // Use the file size as sample_size
//            }
//        )
//
//        println("Response: ${response.bodyAsText()}")
//    } catch (e: Exception) {
//        println("Request failed: ${e.message}")
//    } finally {
//        client.close()
//    }

}

