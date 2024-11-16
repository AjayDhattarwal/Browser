package com.ar.idm.domain.repository

import android.app.Application
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.room.Index
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ar.idm.data.SaveDataWorker
import com.ar.idm.data.local.preferences.BundleStatePreference
import com.ar.idm.data.local.roomdatabase.tabstate.TabDatabase
import com.ar.idm.domain.model.TabState
import com.ar.idm.domain.model.roommodel.TabEntity
import com.ar.idm.utils.function.storeToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream

class TabStateRepository(
    private val context: Application,
    private val tabDatabase: TabDatabase
) {

    private val tabStateDao = tabDatabase.tabDao()

    val bundleStatePreference = BundleStatePreference(context)

    val workManager = WorkManager.getInstance(context)

    suspend fun getAllTab(): List<TabEntity> {
        return withContext(Dispatchers.IO) {
            tabStateDao.getAllTabs()
        }
    }

    suspend fun getTabById(id: String): TabEntity? {
        return withContext(Dispatchers.IO) {
            tabStateDao.getTabByWebViewId(id)
        }
    }

    suspend fun deleteTabById(id: String) {
        withContext(Dispatchers.IO) {
            val work = async { File(context.filesDir,"tab_state${id}.txt").delete() }
            val work2 = async { tabStateDao.delete(id) }
            val work3 = async { File(context.filesDir,"webview_image$id.jpg").delete() }

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