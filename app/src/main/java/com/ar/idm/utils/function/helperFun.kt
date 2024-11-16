package com.ar.idm.utils.function

import android.net.Uri
import android.util.Log
import com.ar.idm.utils.download.FileType
import io.ktor.utils.io.ByteReadChannel
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

fun String.extractString(): String? {
    val regex = "\\((.*?)\\)".toRegex()
    val matchResult = regex.find(this)
    return matchResult?.groups?.get(1)?.value
}

fun String.extractStringBtwBTab(): String? {
    val regex = "<b>(.*?)</b>".toRegex()
    val matchResult = regex.find(this)
    return matchResult?.groups?.get(1)?.value
}


fun String?.sanitizeString(): String? {
    return this
        ?.replace("<b>","")
        ?.replace("</b>","")
        ?.replace("&#39;", "'")
        ?.replace("&nbsp;"," ")
        ?.replace("&quot;", "\"")
        ?.replace("&amp;", "&")
        ?.replace("&lt;", "<")
        ?.replace("&gt;", ">")
        ?.replace("&nbsp;", " ")
        ?.replace("&iexcl;", "¡")
        ?.replace("&cent;", "¢")
        ?.replace("&pound;", "£")
        ?.replace("&curren;", "¤")
        ?.replace("&yen;", "¥")
        ?.replace("&brvbar;", "¦")
        ?.replace("&sect;", "§")
        ?.replace("&uml;", "¨")
        ?.replace("&copy;", "©")
        ?.replace("&reg;", "®")
        ?.replace("&trade;", "™")
        ?.replace("&deg;", "°")
        ?.replace("&plusmn;", "±")
        ?.replace("&times;", "×")
        ?.replace("&divide;", "÷")
        ?.replace("&raquo;", "»")
        ?.replace("&laquo;", "«")
        ?.replace("&bull;", "•")
        ?.replace("&hellip;", "…")
        ?.replace("&shy;", "")

}


fun String?.cleanUrl(): String?{
    return this?.replace("data:text/html,","https://www.google.com")
}


fun generateFaviconUrl(fullUrl: String?): String {
    if(fullUrl == null){
        return "https://www.google.com/favicon.ico"
    }
    val uri = Uri.parse(fullUrl)
    val baseUrl = "${uri.scheme}://${uri.host}"
    return "$baseUrl/favicon.ico"
}


fun Long.asFileSize(): String {
    if (this <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    val digitGroups = (log10(this.toDouble()) /log10(1024.0)).toInt()

    return String.format(Locale.US, "%.1f %s", this / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}



fun Int.asFileSize(): String {
    if (this <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    val digitGroups = (log10(this.toDouble()) /log10(1024.0)).toInt()

    return String.format(Locale.US, "%.1f %s", this / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}



fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
        minutes > 0 -> String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        else -> String.format(Locale.getDefault(), "00:%02d", secs)
    }
}



fun calculateDownloadSpeed(currentSize: Long, startTime: Long): Long {
    val elapsedTime = System.currentTimeMillis() - startTime

    return if (elapsedTime > 0) {
        currentSize * 1000 / elapsedTime
    } else {
        0
    }
}

fun calculateTimeRemaining(currentSize: Long, totalSize: Long, startTime: Long): String {


    if (currentSize < 0 || totalSize <= 0) {
        return "Calculating..."
    }

    val downloadSpeed = calculateDownloadSpeed(currentSize, startTime)

    if (downloadSpeed <= 0) {
        return "Calculating..."
    }

    val remainingBytes = totalSize - currentSize.toLong()

    val timeRemainingSeconds = remainingBytes / downloadSpeed

    return when {
        timeRemainingSeconds < 0 -> "Calculating..."
        timeRemainingSeconds > 24 * 3600 -> "Too long to calculate"
        else -> formatTime(timeRemainingSeconds)
    }
}



fun String?.asFileType(): FileType {
    if(this == null){
        return FileType.OTHER
    }
    return when (this) {
        // Image types
        "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp" -> FileType.IMAGE

        // Audio types
        "audio/mpeg", "audio/wav", "audio/ogg", "audio/aac", "audio/flac" -> FileType.AUDIO

        // Video types
        "video/mp4", "video/x-msvideo", "video/mpeg", "video/quicktime", "video/webm", "application/octet-stream"  -> FileType.VIDEO

        // PDF type
        "application/pdf" -> FileType.PDF

        // Document types
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> FileType.DOCUMENT

        // Spreadsheet types
        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> FileType.SPREADSHEET

        // Compressed types
        "application/zip", "application/x-rar-compressed", "application/x-7z-compressed" -> FileType.COMPRESSED

        // Text types
        "text/plain", "text/html", "text/csv" -> FileType.TEXT

        // Default unknown/other type
        else -> FileType.OTHER
    }
}


fun File.createUniqueFile(): File {
    parentFile?.mkdirs() // Create parent directories if necessary

    // If the file exists, create a new file with an incremented suffix
    if (exists()) {
        var fileNo = 1
        val baseName = name.substringBeforeLast(".")
        val extension = name.substringAfterLast(".", "")

        do {

            val newFileName = "$baseName ($fileNo).$extension"
            val newFile = File(parentFile, newFileName)

            if (!newFile.exists()) {
                newFile.createNewFile()
                return newFile
            }
            fileNo++
        } while (true)
    } else {
        createNewFile()
        return this
    }
}



fun HttpURLConnection.supportResume(): Boolean {
    val range = getHeaderField("Content-Range")
    return range != null && range.startsWith("bytes ")

}

fun extractDomain(url: String): String? {
    // Regex pattern to match the domain part of the URL, excluding 'www.'
    val regex = Regex("^(https?://)?(www\\.)?([^/]+)")

    return regex.find(url)?.groups?.get(3)?.value
}


fun extractUrl(text: String): String? {
    Log.d("extract", text)
    // Split the input text by lines and filter lines starting with "http" or "https"
    val urlPattern = "^https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%.]+".toRegex(RegexOption.MULTILINE)

    // Find the first matching line that starts with "http" or "https"
    return urlPattern.find(text)?.value
}



fun decodeUnicodeEscapes(input: String): String {
    return input.replace(Regex("""\\u([0-9A-Fa-f]{4})""")) {
        val charCode = it.groupValues[1].toInt(16)
        charCode.toChar().toString()
    }
}

// Get current date in yyyy-MM-dd format
fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}
