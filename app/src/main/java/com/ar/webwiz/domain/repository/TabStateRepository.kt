package com.ar.webwiz.domain.repository

import android.app.Application
import android.os.Bundle
import android.os.Parcelable
import android.webkit.WebView
import androidx.compose.ui.graphics.ImageBitmap
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ar.webwiz.data.SaveDataWorker
import com.ar.webwiz.data.local.preferences.BundleStatePreference
import com.ar.webwiz.utils.function.storeToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.io.File


class TabStateRepository(
    private val context: Application,
) {

    private val bundleStatePreference = BundleStatePreference(context)

    private val workManager = WorkManager.getInstance(context)

    suspend fun getAllTab(): List<TabKey> {
        return withContext(Dispatchers.IO) {
           bundleStatePreference.getTabsFromPreferences()
        }
    }


    suspend fun deleteTabById(tag: String) {
        withContext(Dispatchers.IO) {
            val work = async { File(context.filesDir,"tab_state${tag}.bin").delete() }
            val work2 = async { bundleStatePreference.removeTabByTag(tag) }
            val work3 = async { File(context.filesDir,"webview_image${tag}.jpg").delete() }

            work.await()
            work2.await()
            work3.await()
        }
    }




    suspend fun updateSingleTab(webView: WebView, index: Int, overlay: Boolean, imageBitmap: ImageBitmap? = null) {
        val bundle = Bundle()
        webView.saveState(bundle)
        val title = webView.title ?: ""
        val cacheDir = context.filesDir.absolutePath
        val webViewTag = webView.tag.toString()
        var thumbnail: String ? =  null

        withContext(Dispatchers.IO){

            if(imageBitmap != null){
                val file = File(context.filesDir, "webview_image$webViewTag.jpg")

                imageBitmap.storeToFile(
                    file = file
                )
                thumbnail = file.absolutePath
            }
            bundleStatePreference.updateWebViewState(bundle, webView.tag.toString())

            val inputData = Data.Builder()
                .putString("webViewTag", webViewTag)
                .putInt("index", index)
                .putString("title", title)
                .putString("cacheDir", cacheDir)
                .putString("thumbnail", thumbnail)
                .putBoolean("overlay", overlay)
                .build()

            val saveDataWorkRequest = OneTimeWorkRequest.Builder(SaveDataWorker::class.java)
                .setInputData(inputData)
                .addTag(webView.tag.toString())
                .build()

            workManager.enqueue(saveDataWorkRequest)
        }
    }

    fun updateCurrentTabState(
        index: Int,
        title: String,
        cacheDir: String,
        webViewTag: String,
        overlay: Boolean
    ) {

        val inputData = Data.Builder()
            .putString("webViewTag", webViewTag)
            .putInt("index", index)
            .putString("title", title)
            .putString("cacheDir", cacheDir)
            .putBoolean("overlay", overlay)
            .build()

        val saveDataWorkRequest = OneTimeWorkRequest.Builder(SaveDataWorker::class.java)
            .setInputData(inputData)
            .addTag(webViewTag)
            .build()

        workManager.enqueue(saveDataWorkRequest)


    }
}



@Parcelize
data class TabInfo(
    val index: Int,
    val webViewTag: String,
    val title: String? = null,
    val image: String? = null,
    val webViewState: Bundle?,
    val overlay: Boolean
) : Parcelable


data class TabKey(val index: Int, val tag: String)