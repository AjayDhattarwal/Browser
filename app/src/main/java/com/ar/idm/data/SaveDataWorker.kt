package com.ar.idm.data

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import com.ar.idm.data.local.preferences.BundleStatePreference
import com.ar.idm.data.local.roomdatabase.tabstate.TabDatabase
import com.ar.idm.domain.model.roommodel.TabEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

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

                Log.d("SaveDataWorker", "doWork: $thumbnail")


                val bundleString = bundleStatePreference.getBundleStateString(webViewTag)
                    ?: return@withContext Result.failure()


                val db = TabDatabase.getDatabase(applicationContext)
                val tabDao = db.tabDao()


                val file = File(chc, "tab_state${webViewTag}.txt")

                file.writeText(bundleString)

                val tabState = TabEntity(
                    index = index,
                    webViewId = webViewTag,
                    title = title ?: "",
                    stateFilePath = file.absolutePath,
                    thumbnail = thumbnail ?: "webview_image",
                    overlay = overlay
                )

                tabDao.insertTab(tabState)

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
