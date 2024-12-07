package com.ar.webwiz.domain.repository

import com.ar.webwiz.data.local.roomdatabase.historyDb.HistoryDatabase
import com.ar.webwiz.data.local.roomdatabase.historyDb.HistoryItemEntity
import com.ar.webwiz.utils.function.generateFaviconUrl
import com.ar.webwiz.utils.function.getCurrentDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryRepository(
    private val historyDatabase: HistoryDatabase
) {
    private val historyDao = historyDatabase.historyDao()
    private val previousHistoryUrl = MutableStateFlow<List<String>>(emptyList())

    fun addHistory(title : String, url : String, timestamp : Long) {
        if (url == "https://www.google.com/") return

        val currentDate = getCurrentDate()
        CoroutineScope(Dispatchers.IO).launch {
            val existingHistory = historyDao.getHistoryItemForDate(url, currentDate)

            if (!previousHistoryUrl.value.contains(url) && existingHistory == null) {
                previousHistoryUrl.value += url
                historyDao.insert(
                    HistoryItemEntity(
                        title = title,
                        url = url,
                        imageUrl = generateFaviconUrl(url),
                        timestamp = timestamp
                    )
                )
            }else{
                if (existingHistory != null) {
                    historyDao.updateHistoryItem(
                        existingHistory.copy(
                            timestamp = timestamp
                        )
                    )
                }
            }
        }

    }

    suspend fun getHistory(): Flow<List<Pair<String, List<HistoryItemEntity>>>> = withContext(Dispatchers.IO){
        return@withContext historyDao.getAllByFlow().map{ value ->
            value.groupBy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) }
                .map { it.key to it.value }
                .sortedByDescending { it.first }
        }
    }

    fun deleteAllHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            historyDao.deleteAll()
        }
    }

    suspend fun deleteHistory(id: Long) {
        withContext(Dispatchers.IO) {
            val url =  historyDao.getHistoryItemsForId(id).url
            previousHistoryUrl.value = previousHistoryUrl.value.filter { it != url }
            historyDao.deleteById(id)
        }
    }

    fun clearRangeOfHistory(startTimestamp: Long, endTimestamp: Long) {
        CoroutineScope(Dispatchers.IO).launch{
            historyDao.deleteRange(startTimestamp, endTimestamp)
        }
    }


}