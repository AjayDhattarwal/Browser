package com.ar.webwiz.data.local.roomdatabase.historyDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    fun getAllByFlow(): Flow<List<HistoryItemEntity>>

    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    suspend fun getAll(): List<HistoryItemEntity>

    @Query("SELECT * FROM history_items WHERE id = :id ")
    suspend fun getHistoryItemsForId(id: Long): HistoryItemEntity

    @Insert
    suspend fun insert(entity: HistoryItemEntity)

    @Query("DELETE FROM history_items")
    suspend fun deleteAll()

    @Query("DELETE FROM history_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun deleteRange(from: Long, to: Long) {
        val items = getAll()
        val itemsToDelete = items.filter { it.timestamp in from..to }
        itemsToDelete.forEach {
            deleteById(it.id)
        }
    }
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateHistoryItem(historyItem: HistoryItemEntity)

    @Query("SELECT * FROM history_items WHERE url = :url AND strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch') = :date LIMIT 1")
    suspend fun getHistoryItemForDate(url: String, date: String): HistoryItemEntity?



    @Query("DELETE FROM history_items WHERE timestamp < :threshold")
    suspend fun pruneOldHistoryItems(threshold: Long)
}