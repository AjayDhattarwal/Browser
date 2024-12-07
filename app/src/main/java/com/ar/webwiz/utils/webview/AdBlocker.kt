package com.ar.webwiz.utils.webview

import android.content.Context
import android.webkit.WebResourceResponse
import com.ar.webwiz.utils.function.extractDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class AdBlocker(private val context: Context) {
    private val adHostsMaps = List(30) { HashMap<String, Boolean>() }

    private suspend fun loadAdListFromFile(fileName: String, adHosts: HashMap<String, Boolean>) {
    withContext(Dispatchers.IO) {
        context.assets.open(fileName).bufferedReader().use { reader ->
            reader.lineSequence()
                .filter { it.isNotBlank() }
                .forEach { adHosts[it.trim()] = true }
        }
    }
}

    suspend fun initialize() {
        withContext(Dispatchers.IO){
            val files = listOf(
                "adBlockFile_1.txt","adBlockFile_2.txt","adBlockFile_3.txt","adBlockFile_4.txt","adBlockFile_5.txt",
                "adBlockFile_6.txt","adBlockFile_7.txt","adBlockFile_8.txt","adBlockFile_9.txt","adBlockFile_10.txt",
                "adBlockFile_11.txt","adBlockFile_12.txt","adBlockFile_13.txt","adBlockFile_14.txt","adBlockFile_15.txt",
                "adBlockFile_16.txt","adBlockFile_17.txt","adBlockFile_18.txt","adBlockFile_19.txt","adBlockFile_20.txt",
                "adBlockFile_21.txt","adBlockFile_22.txt","adBlockFile_23.txt","adBlockFile_24.txt","adBlockFile_25.txt",
                "adBlockFile_26.txt","adBlockFile_27.txt","adBlockFile_28.txt","adBlockFile_29.txt","adBlockFile_30.txt"
            )
            val deferreds = files.mapIndexed { index, fileName ->
                async { loadAdListFromFile(fileName, adHostsMaps[index]) }
            }
            deferreds.awaitAll()
            println("Ad host loading completed")
        }
    }



    suspend fun isAd(url: String): Boolean = withContext(Dispatchers.IO) {
        val domain = extractDomain(url)
        val checks = adHostsMaps.map { map ->
            async {
                map.containsKey(domain)
            }
        }
        return@withContext checks.awaitAll().any { it }
    }






    fun createEmptyResource(): WebResourceResponse {
        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
    }
}























