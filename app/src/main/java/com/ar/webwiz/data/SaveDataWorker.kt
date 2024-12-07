package com.ar.webwiz.data

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import com.ar.webwiz.data.local.preferences.BundleStatePreference
import com.ar.webwiz.data.local.roomdatabase.tabstate.TabDatabase
import com.ar.webwiz.domain.model.roommodel.TabEntity
import com.ar.webwiz.domain.repository.TabInfo
import com.ar.webwiz.domain.repository.TabKey
import com.ar.webwiz.utils.function.stringToBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SaveDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val bundleStatePreference = BundleStatePreference(context)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val index = inputData.getInt("index", -1)
                val title = inputData.getString("title")
                val webViewTag = inputData.getString("webViewTag") ?: return@withContext Result.failure()
                val chc = inputData.getString("cacheDir")
                val thumbnail = inputData.getString("thumbnail")
                val overlay = inputData.getBoolean("overlay", false)


                val bundleString = bundleStatePreference.getBundleStateString(webViewTag)
                    ?: return@withContext Result.failure()

                val bundle = stringToBundle(bundleString)


                val db = TabDatabase.getDatabase(applicationContext)
                val tabDao = db.tabDao()


                val file = File(chc, "tab_state${webViewTag}.bin")

                val tab = TabInfo(
                    index = index,
                    webViewTag = webViewTag,
                    title = title,
                    image = thumbnail,
                    webViewState = bundle,
                    overlay = overlay
                )

                val parcel = android.os.Parcel.obtain()

                try {
                    parcel.writeParcelable(tab,0)
                    parcel.setDataPosition(0)
                    FileOutputStream(file).use { it.write(parcel.marshall()) }
                } finally {
                    parcel.recycle()
                }


                val tabState = TabEntity(
                    index = index,
                    webViewId = webViewTag,
                    title = title ?: "",
                    stateFilePath = file.absolutePath,
                    thumbnail = thumbnail ?: "webview_image",
                    overlay = overlay
                )

                tabDao.insertTab(tabState)

                bundleStatePreference.addTabToPreferences(TabKey(index, webViewTag))
                bundleStatePreference.deleteWebViewState(webViewTag)



                return@withContext Result.success()
            }catch (e: Exception){
                e.printStackTrace()
                println("Error in SaveDataWorker: ${e.message}")
                return@withContext Result.failure()
            }
        }
    }


}
